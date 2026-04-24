import { useEffect, useState } from 'react';
import { ticketAPI, resourceAPI } from '../api/services';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import './Pages.css';

/**
 * Tickets page (Module C).
 * Users can create incident tickets, view status, add comments, and upload attachments.
 */
export default function Tickets() {
  const { user } = useAuth();
  const toast = useToast();
  const [tickets, setTickets] = useState([]);
  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [comment, setComment] = useState('');
  const [filters, setFilters] = useState({ status: '', priority: '' });
  const [form, setForm] = useState({
    resourceId: '',
    category: '',
    description: '',
    priority: 'MEDIUM',
  });
  const [files, setFiles] = useState([]);

  const fetchTickets = async () => {
    setLoading(true);
    try {
      const params = {};
      if (filters.status) params.status = filters.status;
      if (filters.priority) params.priority = filters.priority;
      const res = await ticketAPI.getAll(params);
      setTickets(res.data);
    } catch (err) {
      console.error('Failed to fetch tickets:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
    resourceAPI.getAll().then(res => setResources(res.data)).catch(() => {});
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      const formData = new FormData();
      if (form.resourceId) formData.append('resourceId', form.resourceId);
      formData.append('category', form.category);
      formData.append('description', form.description);
      formData.append('priority', form.priority);
      files.forEach((file) => formData.append('files', file));

      await ticketAPI.create(formData);
      toast.success('Ticket created successfully!');
      setShowModal(false);
      setForm({ resourceId: '', category: '', description: '', priority: 'MEDIUM' });
      setFiles([]);
      fetchTickets();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create ticket');
    }
  };

  const handleViewTicket = async (ticketId) => {
    try {
      const res = await ticketAPI.getById(ticketId);
      setSelectedTicket(res.data);
    } catch (err) {
      toast.error('Failed to load ticket details');
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!comment.trim()) return;
    try {
      await ticketAPI.addComment(selectedTicket.ticketId, { content: comment });
      toast.success('Comment added');
      setComment('');
      handleViewTicket(selectedTicket.ticketId);
    } catch (err) {
      toast.error('Failed to add comment');
    }
  };

  const handleDeleteComment = async (commentId) => {
    try {
      await ticketAPI.deleteComment(selectedTicket.ticketId, commentId);
      toast.success('Comment deleted');
      handleViewTicket(selectedTicket.ticketId);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete comment');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Incident Tickets</h1>
          <p className="page-subtitle">Report and track maintenance issues</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          ➕ New Ticket
        </button>
      </div>

      {/* Filters */}
      <div className="filter-bar">
        <div className="form-group">
          <label className="form-label">Status</label>
          <select className="form-select" value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
            <option value="">All</option>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CLOSED">Closed</option>
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Priority</label>
          <select className="form-select" value={filters.priority} onChange={(e) => setFilters({ ...filters, priority: e.target.value })}>
            <option value="">All</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
          </select>
        </div>
        <div className="form-group" style={{ justifyContent: 'flex-end' }}>
          <button className="btn btn-secondary" onClick={fetchTickets}>Apply</button>
        </div>
      </div>

      {/* Tickets List */}
      {loading ? (
        <div className="grid-2">{[1,2,3,4].map(i => <div key={i} className="skeleton skeleton-card" style={{height:160}} />)}</div>
      ) : tickets.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">🎫</div>
          <div className="empty-state-title">No tickets found</div>
        </div>
      ) : (
        <div className="grid-2">
          {tickets.map((t) => (
            <div key={t.ticketId} className="glass-card ticket-card" onClick={() => handleViewTicket(t.ticketId)} style={{ cursor: 'pointer' }}>
              <div className="flex items-center justify-between" style={{ marginBottom: '0.75rem' }}>
                <span className={`badge badge-${t.priority?.toLowerCase()}`}>{t.priority}</span>
                <span className={`badge badge-${t.status?.toLowerCase()}`}>{t.status}</span>
              </div>
              <h3 style={{ fontSize: 'var(--font-size-base)', fontWeight: 600, marginBottom: '0.5rem' }}>
                #{t.ticketId} — {t.category}
              </h3>
              <p className="text-muted text-sm" style={{ marginBottom: '0.5rem' }}>
                {t.description?.substring(0, 100)}{t.description?.length > 100 ? '...' : ''}
              </p>
              <div className="text-muted" style={{ fontSize: '0.7rem' }}>
                {t.resource?.name && `📍 ${t.resource.name}`}
                {t.assignedTechnician && ` • 🔧 ${t.assignedTechnician.name}`}
                {t.comments?.length > 0 && ` • 💬 ${t.comments.length}`}
                {t.attachments?.length > 0 && ` • 📎 ${t.attachments.length}`}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Ticket Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">Report an Incident</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleCreate}>
              <div className="form-group mb-md">
                <label className="form-label">Related Resource (optional)</label>
                <select className="form-select" value={form.resourceId} onChange={(e) => setForm({ ...form, resourceId: e.target.value })}>
                  <option value="">None / General Issue</option>
                  {resources.map((r) => <option key={r.resourceId} value={r.resourceId}>{r.name}</option>)}
                </select>
              </div>
              <div className="form-group mb-md">
                <label className="form-label">Category *</label>
                <select className="form-select" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} required>
                  <option value="">Select category</option>
                  <option value="Electrical">Electrical</option>
                  <option value="Plumbing">Plumbing</option>
                  <option value="IT Equipment">IT Equipment</option>
                  <option value="Furniture">Furniture</option>
                  <option value="HVAC">HVAC</option>
                  <option value="Safety">Safety</option>
                  <option value="Other">Other</option>
                </select>
              </div>
              <div className="form-group mb-md">
                <label className="form-label">Priority *</label>
                <select className="form-select" value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                </select>
              </div>
              <div className="form-group mb-md">
                <label className="form-label">Description *</label>
                <textarea className="form-textarea" placeholder="Describe the issue in detail..." value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} required />
              </div>
              <div className="form-group mb-md">
                <label className="form-label">Attachments (up to 3 images)</label>
                <input type="file" className="form-input" multiple accept="image/*" onChange={(e) => setFiles(Array.from(e.target.files).slice(0, 3))} />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Submit Ticket</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Ticket Detail Modal */}
      {selectedTicket && (
        <div className="modal-overlay" onClick={() => setSelectedTicket(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 640 }}>
            <div className="modal-header">
              <h2 className="modal-title">Ticket #{selectedTicket.ticketId}</h2>
              <button className="modal-close" onClick={() => setSelectedTicket(null)}>✕</button>
            </div>

            <div className="flex gap-sm mb-md">
              <span className={`badge badge-${selectedTicket.status?.toLowerCase()}`}>{selectedTicket.status}</span>
              <span className={`badge badge-${selectedTicket.priority?.toLowerCase()}`}>{selectedTicket.priority}</span>
            </div>

            <div className="mb-md">
              <strong>Category:</strong> {selectedTicket.category}<br />
              <strong>Description:</strong><br />
              <p className="text-sm" style={{ marginTop: 4, color: 'var(--text-secondary)' }}>{selectedTicket.description}</p>
            </div>

            {selectedTicket.resource && (
              <div className="mb-md text-sm">
                <strong>Resource:</strong> {selectedTicket.resource.name} ({selectedTicket.resource.location})
              </div>
            )}

            {selectedTicket.assignedTechnician && (
              <div className="mb-md text-sm">
                <strong>Assigned to:</strong> {selectedTicket.assignedTechnician.name}
              </div>
            )}

            {/* Attachments */}
            {selectedTicket.attachments?.length > 0 && (
              <div className="mb-md">
                <strong className="text-sm">Attachments:</strong>
                <div className="flex gap-sm mt-md">
                  {selectedTicket.attachments.map((a) => (
                    <a key={a.attachmentId} href={`http://localhost:8080${a.fileUrl}`} target="_blank" rel="noopener noreferrer" className="btn btn-sm btn-secondary">
                      📎 View
                    </a>
                  ))}
                </div>
              </div>
            )}

            {/* Comments */}
            <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem', marginTop: '1rem' }}>
              <strong>Comments ({selectedTicket.comments?.length || 0})</strong>
              <div style={{ marginTop: '0.75rem', maxHeight: 200, overflowY: 'auto' }}>
                {selectedTicket.comments?.map((c) => (
                  <div key={c.commentId} className="comment-item">
                    <div className="flex items-center justify-between">
                      <strong className="text-sm">{c.user?.name}</strong>
                      <div className="flex items-center gap-sm">
                        <span className="text-muted" style={{ fontSize: '0.7rem' }}>
                          {new Date(c.createdAt).toLocaleString()}
                        </span>
                        {c.user?.userId === user?.userId && (
                          <button className="btn btn-sm btn-danger" style={{padding:'2px 6px',fontSize:'0.65rem'}} onClick={() => handleDeleteComment(c.commentId)}>✕</button>
                        )}
                      </div>
                    </div>
                    <p className="text-sm text-muted" style={{ marginTop: 4 }}>{c.content}</p>
                  </div>
                ))}
              </div>

              {/* Add Comment Form */}
              <form onSubmit={handleAddComment} className="flex gap-sm" style={{ marginTop: '0.75rem' }}>
                <input className="form-input" placeholder="Add a comment..." value={comment} onChange={(e) => setComment(e.target.value)} style={{ flex: 1 }} />
                <button type="submit" className="btn btn-primary btn-sm">Send</button>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
