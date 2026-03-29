import apiClient from './apiClient';

export const authService = {
  async login(payload) {
    const { data } = await apiClient.post('/api/auth/login', payload);
    return data;
  },
  async register(payload) {
    const { data } = await apiClient.post('/api/auth/register', payload);
    return data;
  },
  async validate() {
    const { data } = await apiClient.get('/api/auth/validate');
    return data;
  },
};
