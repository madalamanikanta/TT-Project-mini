import api from './api';
import { User, Internship } from '../app/types';

/** Safely normalize skills to a string array regardless of backend shape */
function normalizeSkills(skills: unknown): string[] {
  if (!skills) return [];
  if (Array.isArray(skills)) return (skills as any[]).map(s => String(s).trim()).filter(Boolean);
  if (typeof skills === 'string') return skills.split(/,\s*/).map(s => s.trim()).filter(Boolean);
  return [];
}

function normalizeInternship(item: any): Internship {
  return { ...item, skills: normalizeSkills(item.skills) } as Internship;
}

export interface AdminDashboardData {
  totalUsers: number;
  totalAdmins: number;
  totalStudents: number;
  totalInternships: number;
  recentActivity: Array<{
    action: string;
    user: string;
    time: string;
  }>;
}

export interface SettingsData {
  appName: string;
  version: string;
  maintenanceMode: boolean;
  registrationEnabled: boolean;
  apiRequestsLast24h: number;
}

export const fetchAdminDashboard = async (): Promise<AdminDashboardData> => {
  const response = await api.get('/admin/dashboard');
  return response.data as AdminDashboardData;
};

export const fetchAdminUsers = async (): Promise<User[]> => {
  const response = await api.get('/admin/users');
  return response.data as User[];
};

export const fetchAdminInternships = async (): Promise<Internship[]> => {
  const response = await api.get('/admin/internships');
  const raw = Array.isArray(response.data) ? response.data : response.data?.data ?? [];
  return (raw as any[]).map(normalizeInternship);
};

export const fetchAdminSettings = async (): Promise<SettingsData> => {
  const response = await api.get('/admin/settings');
  return response.data as SettingsData;
};

export const createAdminInternship = async (data: Partial<Internship>): Promise<Internship> => {
  const response = await api.post('/admin/internships', data);
  return normalizeInternship(response.data);
};
