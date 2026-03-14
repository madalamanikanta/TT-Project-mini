import { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';

interface ProtectedRouteProps {
  children: ReactNode;
  /**
   * If set to 'admin', users without the admin role will be redirected to /admin/login.
   */
  requiredRole?: 'admin';
}

export default function ProtectedRoute({ children, requiredRole }: ProtectedRouteProps) {
  const token = localStorage.getItem('token');
  const userJson = localStorage.getItem('user');
  const user = userJson ? JSON.parse(userJson) : null;
  const location = useLocation();

  const redirectPath = requiredRole === 'admin' ? '/admin/login' : '/login';

  if (!token) {
    return <Navigate to={redirectPath} replace state={{ from: location }} />;
  }

  if (requiredRole === 'admin') {
    const role = (user?.role || '').toString().toLowerCase();
    if (role !== 'admin') {
      return <Navigate to={redirectPath} replace state={{ from: location }} />;
    }
  }

  return <>{children}</>;
}
