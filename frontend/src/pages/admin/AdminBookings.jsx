import { useEffect, useState } from 'react';
import { bookingAPI } from '../../api/services';
import { useToast } from '../../context/ToastContext';
import '../Pages.css';

/**
 * Admin Booking Review page.
 * Admins can view all bookings and approve/reject pending requests.
 */
export default function AdminBookings() {
  const toast = useToast();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [reviewModal, setReviewModal] = useState(null);
  const [reason, setReason] = useState('');

  const fetchBookings = async () => {
    setLoading(true);
    try {
      const res = await bookingAPI.getAll();
      setBookings(res.data);
    } catch (err) {
      console.error('Failed to fetch:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchBookings(); }, []);

  const handleApprove = async (id) => {
    try {
      await bookingAPI.approve(id, { adminReason: reason || null });
      toast.success('Booking approved');
      setReviewModal(null);
      setReason('');
      fetchBookings();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to approve');
    }
  };

  const handleReject = async (id) => {
    try {
      await bookingAPI.reject(id, { adminReason: reason || 'Rejected by admin' });
      toast.success('Booking rejected');
      setReviewModal(null);
      setReason('');
      fetchBookings();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to reject');
    }
  };

  const pendingCount = bookings.filter(b => b.status === 'PENDING').length;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Review Bookings</h1>
          <p className="page-subtitle">{pendingCount} booking{pendingCount !== 1 ? 's' : ''} pending review</p>
        </div>
      </div>

      {loading ? (
        <div>{[1,2,3].map(i => <div key={i} className="skeleton" style={{height:50,marginBottom:8}} />)}</div>
      ) : (
        <div className="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
          <table className="data-table">
            <thead>
              <tr><th>User</th><th>Resource</th><th>Date & Time</th><th>Purpose</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <tr key={b.bookingId}>
                  <td style={{fontWeight:600,color:'var(--text-primary)'}}>{b.user?.name}</td>
                  <td>{b.resource?.name}</td>
                  <td>
                    <div>{new Date(b.startTime).toLocaleDateString()}</div>
                    <div className="text-muted" style={{fontSize:'0.7rem'}}>
                      {new Date(b.startTime).toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'})} - {new Date(b.endTime).toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'})}
                    </div>
                  </td>
                  <td className="text-sm">{b.purpose || '-'}</td>
                  <td><span className={`badge badge-${b.status?.toLowerCase()}`}>{b.status}</span></td>
                  <td>
                    {b.status === 'PENDING' && (
                      <div className="table-actions">
                        <button className="btn btn-sm btn-success" onClick={() => setReviewModal({ booking: b, action: 'approve' })}>Approve</button>
                        <button className="btn btn-sm btn-danger" onClick={() => setReviewModal({ booking: b, action: 'reject' })}>Reject</button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Review Modal */}
      {reviewModal && (
        <div className="modal-overlay" onClick={() => setReviewModal(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">
                {reviewModal.action === 'approve' ? '✅ Approve' : '❌ Reject'} Booking
              </h2>
              <button className="modal-close" onClick={() => setReviewModal(null)}>✕</button>
            </div>
            <p className="text-sm mb-md">
              <strong>{reviewModal.booking.user?.name}</strong> wants to book{' '}
              <strong>{reviewModal.booking.resource?.name}</strong>
            </p>
            <div className="form-group">
              <label className="form-label">Reason (optional)</label>
              <textarea className="form-textarea" value={reason} onChange={(e) => setReason(e.target.value)} placeholder="Add a reason for your decision..." />
            </div>
            <div className="modal-actions">
              <button className="btn btn-secondary" onClick={() => setReviewModal(null)}>Cancel</button>
              {reviewModal.action === 'approve' ? (
                <button className="btn btn-success" onClick={() => handleApprove(reviewModal.booking.bookingId)}>Approve</button>
              ) : (
                <button className="btn btn-danger" onClick={() => handleReject(reviewModal.booking.bookingId)}>Reject</button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
