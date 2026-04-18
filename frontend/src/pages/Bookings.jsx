import { useEffect, useState } from 'react';
import { bookingAPI, resourceAPI } from '../api/services';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import './Pages.css';

/**
 * Bookings page (Module B).
 * Users can view their bookings, create new ones, and cancel existing ones.
 */
export default function Bookings() {
  const { user } = useAuth();
  const toast = useToast();
  const [bookings, setBookings] = useState([]);
  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [filters, setFilters] = useState({ status: '' });
  const [form, setForm] = useState({
    resourceId: '',
    startTime: '',
    endTime: '',
    purpose: '',
  });

  const fetchBookings = async () => {
    setLoading(true);
    try {
      const params = {};
      if (filters.status) params.status = filters.status;
      const res = await bookingAPI.getAll(params);
      setBookings(res.data);
    } catch (err) {
      console.error('Failed to fetch bookings:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchResources = async () => {
    try {
      const res = await resourceAPI.getAll({ status: 'ACTIVE' });
      setResources(res.data);
    } catch (err) {
      console.error('Failed to fetch resources:', err);
    }
  };

  useEffect(() => {
    fetchBookings();
    fetchResources();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await bookingAPI.create({
        resourceId: parseInt(form.resourceId),
        startTime: form.startTime,
        endTime: form.endTime,
        purpose: form.purpose,
      });
      toast.success('Booking created successfully!');
      setShowModal(false);
      setForm({ resourceId: '', startTime: '', endTime: '', purpose: '' });
      fetchBookings();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create booking');
    }
  };

  const handleCancel = async (id) => {
    if (!confirm('Are you sure you want to cancel this booking?')) return;
    try {
      await bookingAPI.cancel(id);
      toast.success('Booking cancelled');
      fetchBookings();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to cancel booking');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">My Bookings</h1>
          <p className="page-subtitle">Manage your resource reservations</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          ➕ New Booking
        </button>
      </div>

      {/* Filter */}
      <div className="filter-bar">
        <div className="form-group">
          <label className="form-label">Status</label>
          <select className="form-select" value={filters.status} onChange={(e) => setFilters({ status: e.target.value })}>
            <option value="">All Status</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
        <div className="form-group" style={{ justifyContent: 'flex-end' }}>
          <button className="btn btn-secondary" onClick={fetchBookings}>Apply Filter</button>
        </div>
      </div>

      {/* Bookings Table */}
      {loading ? (
        <div>{[1,2,3].map(i => <div key={i} className="skeleton skeleton-card" style={{height:60,marginBottom:8}} />)}</div>
      ) : bookings.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📅</div>
          <div className="empty-state-title">No bookings found</div>
          <p>Create your first booking to get started</p>
        </div>
      ) : (
        <div className="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Resource</th>
                <th>Date & Time</th>
                <th>Purpose</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <tr key={b.bookingId}>
                  <td style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{b.resource?.name}</td>
                  <td>
                    <div>{new Date(b.startTime).toLocaleDateString()}</div>
                    <div className="text-muted" style={{ fontSize: '0.75rem' }}>
                      {new Date(b.startTime).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})} - {new Date(b.endTime).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}
                    </div>
                  </td>
                  <td>{b.purpose || '-'}</td>
                  <td>
                    <span className={`badge badge-${b.status?.toLowerCase()}`}>{b.status}</span>
                    {b.adminReason && (
                      <div className="text-muted" style={{ fontSize: '0.7rem', marginTop: 4 }}>
                        Reason: {b.adminReason}
                      </div>
                    )}
                  </td>
                  <td>
                    {(b.status === 'PENDING' || b.status === 'APPROVED') && (
                      <button className="btn btn-danger btn-sm" onClick={() => handleCancel(b.bookingId)}>
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Create Booking Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">New Booking</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleCreate}>
              <div className="form-group mb-md">
                <label className="form-label">Resource *</label>
                <select className="form-select" value={form.resourceId} onChange={(e) => setForm({ ...form, resourceId: e.target.value })} required>
                  <option value="">Select a resource</option>
                  {resources.map((r) => (
                    <option key={r.resourceId} value={r.resourceId}>
                      {r.name} ({r.type}) - {r.location}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group mb-md">
                <label className="form-label">Start Time *</label>
                <input type="datetime-local" className="form-input" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} required />
              </div>
              <div className="form-group mb-md">
                <label className="form-label">End Time *</label>
                <input type="datetime-local" className="form-input" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} required />
              </div>
              <div className="form-group mb-md">
                <label className="form-label">Purpose</label>
                <textarea className="form-textarea" placeholder="Describe the purpose of your booking..." value={form.purpose} onChange={(e) => setForm({ ...form, purpose: e.target.value })} />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Create Booking</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
