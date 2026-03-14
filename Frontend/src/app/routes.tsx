import { createBrowserRouter } from 'react-router';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import StudentDashboard from './pages/student/Dashboard';
import ProfilePage from './pages/student/Profile';
import MatchedInternships from './pages/student/MatchedInternships';
import SavedInternships from './pages/student/SavedInternships';
import InternshipListing from './pages/InternshipListing';
import InternshipDetail from './pages/InternshipDetail';
import AdminDashboard from './pages/admin/Dashboard';
import AdminLogin from './pages/admin/Login';
import ManageInternships from './pages/admin/ManageInternships';
import ManageUsers from './pages/admin/ManageUsers';
import ProtectedRoute from './components/ProtectedRoute';

export const router = createBrowserRouter([
  {
    path: '/',
    Component: LandingPage,
  },
  {
    path: '/login',
    Component: LoginPage,
  },
  {
    path: '/register',
    Component: RegisterPage,
  },
  {
    path: '/internships',
    Component: InternshipListing,
  },
  {
    path: '/internships/:id',
    Component: InternshipDetail,
  },
  {
    path: '/student/dashboard',
    element: (
      <ProtectedRoute>
        <StudentDashboard />
      </ProtectedRoute>
    ),
  },
  {
    path: '/student/profile',
    element: (
      <ProtectedRoute>
        <ProfilePage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/student/matches',
    element: (
      <ProtectedRoute>
        <MatchedInternships />
      </ProtectedRoute>
    ),
  },
  {
    path: '/student/saved',
    element: (
      <ProtectedRoute>
        <SavedInternships />
      </ProtectedRoute>
    ),
  },
  {
    path: '/admin/login',
    Component: AdminLogin,
  },
  {
    path: '/admin/dashboard',
    element: (
      <ProtectedRoute requiredRole="admin">
        <AdminDashboard />
      </ProtectedRoute>
    ),
  },
  {
    path: '/admin/internships',
    element: (
      <ProtectedRoute requiredRole="admin">
        <ManageInternships />
      </ProtectedRoute>
    ),
  },
  {
    path: '/admin/users',
    element: (
      <ProtectedRoute requiredRole="admin">
        <ManageUsers />
      </ProtectedRoute>
    ),
  },
]);