import { useEffect, useState } from 'react';
import { notificationAPI } from '../api/services';
import { useToast } from '../context/ToastContext';
import './Pages.css';

/**
 * Notifications page (Module D).
 * Users can view, mark as read, and manage their notifications.
 * @author mohsh
 */
export default function Notifications() {
  const toast = useToast();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const res = await notificationAPI.getAll();
      setNotifications(res.data);
    } catch (err) {
      console.error('Failed to fetch notifications:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchNotifications(); }, []);

  const handleMarkRead = async (id) => {
    try {
      await notificationAPI.markAsRead(id);
      fetchNotifications();
    } catch (err) {
      toast.error('Failed to mark as read');
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationAPI.markAllAsRead();
      toast.success('All notifications marked as read');
      fetchNotifications();
    } catch (err) {
      toast.error('Failed to mark all as read');
    }
  };

  const handleDelete = async (id) => {
    try {
      await notificationAPI.delete(id);
      fetchNotifications();
    } catch (err) {
      toast.error('Failed to delete notification');
    }
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Notifications</h1>
          <p className="page-subtitle">{unreadCount} unread notification{unreadCount !== 1 ? 's' : ''}</p>
        </div>
        {unreadCount > 0 && (
          <button className="btn btn-secondary" onClick={handleMarkAllRead}>
            ✓ Mark All as Read
          </button>
        )}
      </div>

      {loading ? (
        <div>{[1,2,3,4].map(i => <div key={i} className="skeleton" style={{height:60,marginBottom:8,borderRadius:10}} />)}</div>
      ) : notifications.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">🔔</div>
          <div className="empty-state-title">No notifications</div>
          <p>You're all caught up!</p>
        </div>
      ) : (
        <div className="notifications-list">
          {notifications.map((n) => (
            <div key={n.notificationId} className={`notification-item glass-card ${!n.isRead ? 'unread' : ''}`}>
              <div className="notification-dot" style={{ opacity: n.isRead ? 0 : 1 }} />
              <div className="notification-content">
                <p className="notification-message">{n.message}</p>
                <span className="notification-time">
                  {new Date(n.createdAt).toLocaleString()}
                </span>
              </div>
              <div className="notification-actions">
                {!n.isRead && (
                  <button className="btn btn-sm btn-secondary" onClick={() => handleMarkRead(n.notificationId)} title="Mark as read">
                    ✓
                  </button>
                )}
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(n.notificationId)} title="Delete" style={{padding:'4px 8px'}}>
                  ✕
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
