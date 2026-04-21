import { useEffect, useState } from 'react';
import { ticketAPI, authAPI } from '../../api/services';
import { useToast } from '../../context/ToastContext';
import '../Pages.css';

/**
 * Admin Ticket Management page.
 * Admins can manage all tickets: assign technicians, update status.
 */
export default function AdminTickets() {
  const toast = useToast();
  const [tickets, setTickets] = useState([]);
  const [technicians, setTechnicians] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionModal, setActionModal] = useState(null);
  const [form, setForm] = useState({ status: '', assignedTechnicianId: '' });

  const fetchTickets = async () => {
    setLoading(true);
    try {
      const res = await ticketAPI.getAll();
      setTickets(res.data);
    } catch (err) {
      console.error('Failed to fetch:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
    authAPI.getAllUsers().then(res => {
      setTechnicians(res.data.filter(u => u.role === 'TECHNICIAN' || u.role === 'ADMIN'));
    }).catch(() => {});
  }, []);

  const handleUpdate = async () => {
    try {
      const data = {};
      if (form.status) data.status = form.status;
      if (form.assignedTechnicianId) data.assignedTechnicianId = parseInt(form.assignedTechnicianId);

      await ticketAPI.update(actionModal.ticketId, data);
      toast.success('Ticket updated');
      setActionModal(null);
      fetchTickets();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update');
    }
  };

  const openAction = (ticket) => {
    setForm({
      status: '',
      assignedTechnicianId: ticket.assignedTechnician?.userId || '',
    });
    setActionModal(ticket);
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Manage Tickets</h1>
          <p className="page-subtitle">Assign technicians and update ticket status</p>
        </div>
      </div>

      {loading ? (
        <div>{[1,2,3].map(i => <div key={i} className="skeleton" style={{height:50,marginBottom:8}} />)}</div>
      ) : (
        <div className="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
          <table className="data-table">
            <thead>
              <tr><th>#</th><th>Reporter</th><th>Category</th><th>Priority</th><th>Status</th><th>Assigned To</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {tickets.map((t) => (
                <tr key={t.ticketId}>
                  <td>#{t.ticketId}</td>
                  <td style={{fontWeight:600,color:'var(--text-primary)'}}>{t.user?.name}</td>
                  <td>{t.category}</td>
                  <td><span className={`badge badge-${t.priority?.toLowerCase()}`}>{t.priority}</span></td>
                  <td><span className={`badge badge-${t.status?.toLowerCase()}`}>{t.status}</span></td>
                  <td>{t.assignedTechnician?.name || <span className="text-muted">Unassigned</span>}</td>
                  <td>
                    <button className="btn btn-sm btn-secondary" onClick={() => openAction(t)}>Manage</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Update Modal */}
      {actionModal && (
        <div className="modal-overlay" onClick={() => setActionModal(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">Manage Ticket #{actionModal.ticketId}</h2>
              <button className="modal-close" onClick={() => setActionModal(null)}>✕</button>
            </div>
            <p className="text-sm mb-md"><strong>{actionModal.category}</strong>: {actionModal.description?.substring(0, 100)}</p>

            <div className="form-group">
              <label className="form-label">Update Status</label>
              <select className="form-select" value={form.status} onChange={(e) => setForm({...form, status: e.target.value})}>
                <option value="">No Change</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="RESOLVED">Resolved</option>
                <option value="CLOSED">Closed</option>
                <option value="REJECTED">Rejected</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Assign Technician</label>
              <select className="form-select" value={form.assignedTechnicianId} onChange={(e) => setForm({...form, assignedTechnicianId: e.target.value})}>
                <option value="">Unassigned</option>
                {technicians.map(t => (
                  <option key={t.userId} value={t.userId}>{t.name} ({t.role})</option>
                ))}
              </select>
            </div>

            <div className="modal-actions">
              <button className="btn btn-secondary" onClick={() => setActionModal(null)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleUpdate}>Update Ticket</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
