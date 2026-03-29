import apiClient from './apiClient';

export const monsterService = {
  async getPlayerMonsters() {
    const { data } = await apiClient.get('/api/player/monsters');
    return data;
  },
  async getMonsterById(id) {
    const { data } = await apiClient.get(`/api/monsters/${id}`);
    return data;
  },
};
