import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState, useEffect } from 'react';
import { notificationAPI } from '../api/services';
import './Layout.css';
import logo from '../assets/logo.png';

/**
 * Main Layout component with sidebar navigation.
 * Renders different navigation links based on user role.
 */
export default function Layout() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);

  // Fetch unread notification count
  useEffect(() => {
    const fetchUnread = async () => {
      try {
        const res = await notificationAPI.getUnreadCount();
        setUnreadCount(res.data.count);
      } catch (err) {
        console.error('Failed to fetch notifications:', err);
      }
    };
    fetchUnread();
    // Poll every 30 seconds
    const interval = setInterval(fetchUnread, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getInitials = (name) => {
    if (!name) return '?';
    return name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2);
  };

  return (
    <div className="layout">
      {/* Sidebar Navigation */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <div className="sidebar-logo-icon">
              <img src={logo} alt="CampusHub Logo" />
            </div>
            <span className="sidebar-logo-text">CampusHub</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          {/* Common Navigation */}
          <div className="sidebar-section">
            <div className="sidebar-section-title">Main</div>
            <NavLink to="/dashboard" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
              <span className="sidebar-link-icon">📊</span>Dashboard
            </NavLink>
            <NavLink to="/resources" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
              <span className="sidebar-link-icon">🏢</span>Resources
            </NavLink>
            <NavLink to="/bookings" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
              <span className="sidebar-link-icon">📅</span>Bookings
            </NavLink>
            <NavLink to="/tickets" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
              <span className="sidebar-link-icon">🎫</span>Tickets
            </NavLink>
            <NavLink to="/notifications" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
              <span className="sidebar-link-icon">🔔</span>
              Notifications
              {unreadCount > 0 && (
                <span className="badge badge-open" style={{ marginLeft: 'auto', fontSize: '0.65rem' }}>
                  {unreadCount}
                </span>
              )}
            </NavLink>
          </div>

          {/* Admin-only Navigation */}
          {isAdmin() && (
            <div className="sidebar-section">
              <div className="sidebar-section-title">Admin</div>
              <NavLink to="/admin/resources" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
                <span className="sidebar-link-icon">⚙️</span>Manage Resources
              </NavLink>
              <NavLink to="/admin/bookings" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
                <span className="sidebar-link-icon">✅</span>Review Bookings
              </NavLink>
              <NavLink to="/admin/tickets" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
                <span className="sidebar-link-icon">🔧</span>Manage Tickets
              </NavLink>
              <NavLink to="/admin/users" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
                <span className="sidebar-link-icon">👥</span>User Management
              </NavLink>
            </div>
          )}
        </nav>

        {/* Sidebar Footer with User Info */}
        <div className="sidebar-footer">
          <div className="sidebar-user">
            <div className="sidebar-avatar">
              {user?.profilePicture ? (
                <img src={user.profilePicture} alt={user.name} referrerPolicy="no-referrer" />
              ) : (
                getInitials(user?.name)
              )}
            </div>
            <div className="sidebar-user-info">
              <div className="sidebar-user-name">{user?.name}</div>
              <div className="sidebar-user-role">{user?.role}</div>
            </div>
            <button className="sidebar-logout" onClick={handleLogout} title="Logout">
              🚪
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
