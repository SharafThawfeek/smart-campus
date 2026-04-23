import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { resourceAPI, bookingAPI, ticketAPI, notificationAPI } from '../api/services';
import './Dashboard.css';

/**
 * Dashboard page showing overview statistics and recent activity.
 * Content varies based on user role (USER vs ADMIN).
 */
export default function Dashboard() {
  const { user, isAdmin } = useAuth();
  const [stats, setStats] = useState({
    resources: 0,
    bookings: 0,
    tickets: 0,
    notifications: 0,
  });
  const [recentBookings, setRecentBookings] = useState([]);
  const [recentTickets, setRecentTickets] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [resourcesRes, bookingsRes, ticketsRes, notifRes] = await Promise.all([
          resourceAPI.getAll(),
          bookingAPI.getAll(),
          ticketAPI.getAll(),
          notificationAPI.getUnreadCount(),
        ]);

        setStats({
          resources: resourcesRes.data.length,
          bookings: bookingsRes.data.length,
          tickets: ticketsRes.data.length,
          notifications: notifRes.data.count,
        });

        setRecentBookings(bookingsRes.data.slice(0, 5));
        setRecentTickets(ticketsRes.data.slice(0, 5));
      } catch (err) {
        console.error('Dashboard load error:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) {
    return (
      <div>
        <div className="skeleton skeleton-title" />
        <div className="grid-4" style={{ marginTop: '1.5rem' }}>
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="skeleton skeleton-card" />
          ))}
        </div>
      </div>
    );
  }

  const statCards = [
    { label: 'Total Resources', value: stats.resources, icon: '🏢', color: 'var(--primary)', gradient: 'linear-gradient(135deg, rgba(99,102,241,0.15), rgba(99,102,241,0.05))' },
    { label: isAdmin() ? 'All Bookings' : 'My Bookings', value: stats.bookings, icon: '📅', color: 'var(--accent-cyan)', gradient: 'linear-gradient(135deg, rgba(6,182,212,0.15), rgba(6,182,212,0.05))' },
    { label: isAdmin() ? 'All Tickets' : 'My Tickets', value: stats.tickets, icon: '🎫', color: 'var(--accent-amber)', gradient: 'linear-gradient(135deg, rgba(245,158,11,0.15), rgba(245,158,11,0.05))' },
    { label: 'Unread Notifications', value: stats.notifications, icon: '🔔', color: 'var(--accent-rose)', gradient: 'linear-gradient(135deg, rgba(244,63,94,0.15), rgba(244,63,94,0.05))' },
  ];

  return (
    <div className="dashboard">
      <div className="page-header">
        <div>
          <h1 className="page-title">
            {isAdmin() ? 'Admin Dashboard' : 'Dashboard'}
          </h1>
          <p className="page-subtitle">Welcome back, {user?.name} 👋</p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid-4">
        {statCards.map((card, i) => (
          <div key={i} className="stat-card glass-card" style={{ background: card.gradient }}>
            <div className="stat-icon" style={{ color: card.color }}>{card.icon}</div>
            <div className="stat-value">{card.value}</div>
            <div className="stat-label">{card.label}</div>
          </div>
        ))}
      </div>

      {/* Recent Activity */}
      <div className="dashboard-grid" style={{ marginTop: '2rem' }}>
        {/* Recent Bookings */}
        <div className="glass-card">
          <h3 className="card-title">Recent Bookings</h3>
          {recentBookings.length === 0 ? (
            <p className="text-muted text-sm">No bookings yet</p>
          ) : (
            <div className="activity-list">
              {recentBookings.map((b) => (
                <div key={b.bookingId} className="activity-item">
                  <div className="activity-content">
                    <div className="activity-name">{b.resource?.name || 'Unknown'}</div>
                    <div className="activity-meta">
                      {new Date(b.startTime).toLocaleDateString()} • {b.purpose || 'No purpose'}
                    </div>
                  </div>
                  <span className={`badge badge-${b.status?.toLowerCase()}`}>{b.status}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recent Tickets */}
        <div className="glass-card">
          <h3 className="card-title">Recent Tickets</h3>
          {recentTickets.length === 0 ? (
            <p className="text-muted text-sm">No tickets yet</p>
          ) : (
            <div className="activity-list">
              {recentTickets.map((t) => (
                <div key={t.ticketId} className="activity-item">
                  <div className="activity-content">
                    <div className="activity-name">{t.category}</div>
                    <div className="activity-meta">
                      Priority: {t.priority} • {t.description?.substring(0, 50)}...
                    </div>
                  </div>
                  <span className={`badge badge-${t.status?.toLowerCase()}`}>{t.status}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
