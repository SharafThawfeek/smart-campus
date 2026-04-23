import { useEffect, useState } from 'react';
import { resourceAPI } from '../../api/services';
import { useToast } from '../../context/ToastContext';
import '../Pages.css';

/**
 * Admin Resource Management page.
 * Full CRUD: create, view, edit, and delete campus resources.
 */
export default function AdminResources() {
  const toast = useToast();
  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({ name: '', type: 'ROOM', capacity: '', location: '', status: 'ACTIVE' });

  const fetchResources = async () => {
    setLoading(true);
    try {
      const res = await resourceAPI.getAll();
      setResources(res.data);
    } catch (err) {
      console.error('Failed to fetch:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchResources(); }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm({ name: '', type: 'ROOM', capacity: '', location: '', status: 'ACTIVE' });
    setShowModal(true);
  };

  const openEdit = (r) => {
    setEditingId(r.resourceId);
    setForm({ name: r.name, type: r.type, capacity: r.capacity || '', location: r.location || '', status: r.status });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const data = { ...form, capacity: form.capacity ? parseInt(form.capacity) : null };
      if (editingId) {
        await resourceAPI.update(editingId, data);
        toast.success('Resource updated');
      } else {
        await resourceAPI.create(data);
        toast.success('Resource created');
      }
      setShowModal(false);
      fetchResources();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Are you sure you want to delete this resource?')) return;
    try {
      await resourceAPI.delete(id);
      toast.success('Resource deleted');
      fetchResources();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Manage Resources</h1>
          <p className="page-subtitle">Add, edit, and manage campus facilities</p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>➕ Add Resource</button>
      </div>

      {loading ? (
        <div>{[1,2,3].map(i => <div key={i} className="skeleton" style={{height:50,marginBottom:8}} />)}</div>
      ) : (
        <div className="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
          <table className="data-table">
            <thead>
              <tr><th>Name</th><th>Type</th><th>Capacity</th><th>Location</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {resources.map((r) => (
                <tr key={r.resourceId}>
                  <td style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{r.name}</td>
                  <td>{r.type}</td>
                  <td>{r.capacity || '-'}</td>
                  <td>{r.location || '-'}</td>
                  <td><span className={`badge badge-${r.status?.toLowerCase()}`}>{r.status}</span></td>
                  <td>
                    <div className="table-actions">
                      <button className="btn btn-sm btn-secondary" onClick={() => openEdit(r)}>Edit</button>
                      <button className="btn btn-sm btn-danger" onClick={() => handleDelete(r.resourceId)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Create/Edit Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">{editingId ? 'Edit Resource' : 'Add Resource'}</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group"><label className="form-label">Name *</label>
                <input className="form-input" value={form.name} onChange={(e) => setForm({...form, name: e.target.value})} required /></div>
              <div className="form-group"><label className="form-label">Type *</label>
                <select className="form-select" value={form.type} onChange={(e) => setForm({...form, type: e.target.value})}>
                  <option value="ROOM">Room</option><option value="LAB">Lab</option><option value="EQUIPMENT">Equipment</option>
                </select></div>
              <div className="form-group"><label className="form-label">Capacity</label>
                <input type="number" className="form-input" value={form.capacity} onChange={(e) => setForm({...form, capacity: e.target.value})} /></div>
              <div className="form-group"><label className="form-label">Location</label>
                <input className="form-input" value={form.location} onChange={(e) => setForm({...form, location: e.target.value})} /></div>
              <div className="form-group"><label className="form-label">Status</label>
                <select className="form-select" value={form.status} onChange={(e) => setForm({...form, status: e.target.value})}>
                  <option value="ACTIVE">Active</option><option value="OUT_OF_SERVICE">Out of Service</option>
                </select></div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">{editingId ? 'Update' : 'Create'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
