import { Link, useLocation } from 'react-router-dom';
import { 
  LayoutDashboard, 
  User, 
  Briefcase, 
  Heart, 
  Bookmark, 
  Settings,
  Users,
  FileText
} from 'lucide-react';
import { cn } from '../ui/utils';

interface SidebarProps {
  userRole: 'student' | 'admin';
}

const studentMenuItems = [
  { icon: LayoutDashboard, label: 'Dashboard', path: '/student/dashboard' },
  { icon: User, label: 'Profile', path: '/student/profile' },
  { icon: Briefcase, label: 'Internships', path: '/internships' },
  { icon: Heart, label: 'Matches', path: '/student/matches' },
  { icon: Bookmark, label: 'Saved', path: '/student/saved' },
];

const adminMenuItems = [
  { icon: LayoutDashboard, label: 'Dashboard', path: '/admin/dashboard' },
  { icon: Briefcase, label: 'Manage Internships', path: '/admin/internships' },
  { icon: Users, label: 'Manage Users', path: '/admin/users' },
];

export function Sidebar({ userRole }: SidebarProps) {
  const location = useLocation();
  const menuItems = userRole === 'student' ? studentMenuItems : adminMenuItems;

  return (
    <div className="w-64 bg-white border-r min-h-screen sticky top-0">
      <div className="p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-6">
          {userRole === 'student' ? 'Student Portal' : 'Admin Portal'}
        </h2>
        <nav className="space-y-2">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            
            return (
              <Link
                key={item.path}
                to={item.path}
                className={cn(
                  "flex items-center space-x-3 px-4 py-3 rounded-lg transition-colors",
                  isActive
                    ? "bg-primary text-white"
                    : "text-gray-700 hover:bg-gray-100"
                )}
              >
                <Icon className="h-5 w-5" />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>
      </div>
    </div>
  );
}
