import apiClient from './apiClient';

export const invocationService = {
  async summon() {
    const { data } = await apiClient.post('/api/invocations/summon');
    return data;
  },
  async getTemplates() {
    const { data } = await apiClient.get('/api/invocations/templates');
    return data;
  },
  async getHistory() {
    const { data } = await apiClient.get('/api/invocations/history');
    return data;
  },
};
