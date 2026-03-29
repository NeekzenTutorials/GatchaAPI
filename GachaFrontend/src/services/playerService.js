import apiClient from './apiClient';

export const playerService = {
  async getProfile() {
    const { data } = await apiClient.get('/api/player/profile');
    return data;
  },
  async getLevel() {
    const { data } = await apiClient.get('/api/player/level');
    return data;
  },
  async addExperience(amount) {
    const { data } = await apiClient.post('/api/player/xp', { amount });
    return data;
  },
};
