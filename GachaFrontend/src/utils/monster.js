export function inferRarity(value) {
  const src = String(value || '');
  const match = src.match(/(legendary|epic|rare|common)$/i);
  if (match) {
    return match[1][0].toUpperCase() + match[1].slice(1).toLowerCase();
  }
  return 'Common';
}

export function formatElementTypeLabel(value) {
  const raw = String(value || '').trim();
  // Exemples backend rencontrés: "WATERCommon", "EauCommon"
  const v = raw.replace(/(legendary|epic|rare|common)$/i, '').trim().toLowerCase();

  if (/(water|eau)$/.test(v)) return 'Eau';
  if (/(fire|feu)$/.test(v)) return 'Feu';
  if (/(wind|vent)$/.test(v)) return 'Vent';
  if (/(earth|terre)$/.test(v)) return 'Terre';
  if (/(light|lumiere|lumière)$/.test(v)) return 'Lumière';
  if (/(dark|tenebre|ténèbre)$/.test(v)) return 'Ténèbre';
  if (/(neutral|neutre)$/.test(v)) return 'Neutre';

  return v ? v[0].toUpperCase() + v.slice(1) : 'Neutre';
}

export function formatElementWithRarity(value) {
  const raw = String(value || '').trim();
  if (!raw) return 'Neutre';

  const spaced = raw
    .replace(
      /(water|fire|wind|earth|light|dark|neutral|neutre|eau|feu|vent|terre|lumiere|lumière|tenebre|ténèbre)(legendary|epic|rare|common)$/i,
      '$1 $2'
    )
    .replace(/([a-zà-ÿ])([A-ZÀ-Ÿ])/g, '$1 $2')
    .replace(/[_-]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();

  const [elementPart = '', rarityPart = ''] = spaced.split(' ');
  const elementLabel = formatElementTypeLabel(elementPart);
  const rarityLabel = /(legendary|epic|rare|common)/i.test(rarityPart)
    ? rarityPart[0].toUpperCase() + rarityPart.slice(1).toLowerCase()
    : '';

  return `${elementLabel}${rarityLabel ? ` ${rarityLabel}` : ''}`.trim();
}

function stripSuffixCI(value, suffix) {
  if (!suffix) return value;
  const text = String(value || '');
  const end = String(suffix || '');
  const rx = new RegExp(`${end}$`, 'i');
  return text.replace(rx, '');
}

function cleanMonsterName(rawName, elementType, rarity) {
  let name = String(rawName || '');
  if (!name) return 'Monstre inconnu';

  // Cas fréquent backend: LeviathanWATERCommon / LeviathanEauCommon
  const parsed = name.match(
    /^(.*?)(WATER|FIRE|WIND|EARTH|LIGHT|DARK|NEUTRAL|NEUTRE|EAU|FEU|VENT|TERRE|LUMIERE|LUMIÈRE|TENEBRE|TÉNÈBRE)?(LEGENDARY|EPIC|RARE|COMMON)?$/i
  );
  if (parsed && parsed[1]) {
    name = parsed[1];
  }

  // Suppressions ciblées avec les infos déjà normalisées
  name = stripSuffixCI(name, rarity);
  name = stripSuffixCI(name, elementType);

  // Sécurités si elementType n'est pas présent dans la réponse
  name = name.replace(
    /(WATER|FIRE|WIND|EARTH|LIGHT|DARK|NEUTRAL|NEUTRE|EAU|FEU|VENT|TERRE|LUMIERE|LUMIÈRE|TENEBRE|TÉNÈBRE)$/i,
    ''
  );
  name = name.replace(/(LEGENDARY|EPIC|RARE|COMMON)$/i, '');

  // Recase éventuel CamelCase parasite (ex: LeviathanEau)
  name = name.replace(/([a-zà-ÿ])([A-ZÀ-Ÿ])/g, '$1 $2');

  name = name.replace(/[_-]+/g, ' ').trim();
  return name || String(rawName);
}

function buildDisplayName(name, typeLabel) {
  const cleanName = String(name || '').replace(/\s+/g, ' ').trim();
  const cleanType = String(typeLabel || '').replace(/\s+/g, ' ').trim();
  if (!cleanType) return cleanName || 'Monstre inconnu';

  const lowerName = cleanName.toLowerCase();
  const lowerType = cleanType.toLowerCase();

  // Évite les doublons: "Sylph Vent" + "Vent" => "Sylph Vent"
  if (lowerName.endsWith(` ${lowerType}`) || lowerName === lowerType) {
    return cleanName;
  }

  return `${cleanName} ${cleanType}`.trim();
}

export function normalizeMonsterModel(raw = {}, template = null) {
  const source = raw || {};
  const rawName = source.name || source.monsterName || template?.name || 'Monstre inconnu';
  const rarity = source.rarity || source.tier || inferRarity(rawName);
  const type = source.type || source.elementType || template?.elementType || 'neutre';
  const name = cleanMonsterName(rawName, type, rarity);
  const typeLabel = formatElementTypeLabel(type);
  const typeDisplay = formatElementWithRarity(String(type || '') || `${typeLabel}${rarity}`);
  const displayName = buildDisplayName(name, typeLabel);

  return {
    ...source,
    id: source.id || source._id || source.generatedMonsterId || source.monsterId,
    name,
    rawName,
    type,
    typeLabel,
    typeDisplay,
    displayName,
    elementType: source.elementType || type,
    rarity,
    hp: source.hp ?? template?.hp ?? null,
    atk: source.atk ?? template?.atk ?? null,
    def: source.def ?? template?.def ?? null,
    speed: source.speed ?? source.vit ?? template?.vit ?? null,
    vit: source.vit ?? source.speed ?? template?.vit ?? null,
    skills: source.skills || template?.skills || [],
    summonedAt: source.summonedAt || source.createdAt || source.updatedAt || null,
  };
}
