// src/pages/admin/AdminManageUsersPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AdminService from '../../services/admin.service'; // Path to your new admin service
import { useAuth } from '../../contexts/AuthContext';
import Header from '../../components/Header';
import { formatDate } from '../../utils/dateFormatter'; // Assuming you have this utility

// Placeholder for a role editing modal or component - very basic for now
const EditRolesModal = ({ user, availableRoles, onSave, onCancel }) => {
  const [selectedRoles, setSelectedRoles] = useState(new Set(user.roles || []));

  const handleRoleToggle = (roleName) => {
    setSelectedRoles(prev => {
      const newRoles = new Set(prev);
      if (newRoles.has(roleName)) {
        newRoles.delete(roleName);
      } else {
        newRoles.add(roleName);
      }
      return newRoles;
    });
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
      <div className="bg-gray-800 p-6 rounded-lg shadow-xl w-full max-w-md">
        <h3 className="text-xl font-semibold mb-4">Edit Roles for {user.firstName} {user.lastName}</h3>
        <div className="space-y-2 mb-6">
          {availableRoles.map(role => (
            <label key={role} className="flex items-center space-x-2 cursor-pointer">
              <input
                type="checkbox"
                className="form-checkbox h-5 w-5 text-netflix-red bg-gray-700 border-gray-600 rounded focus:ring-netflix-red"
                checked={selectedRoles.has(role)}
                onChange={() => handleRoleToggle(role)}
              />
              <span className="text-gray-300">{role.replace('ROLE_', '')}</span>
            </label>
          ))}
        </div>
        <div className="flex justify-end space-x-3">
          <button onClick={onCancel} className="px-4 py-2 text-sm rounded-md bg-gray-600 hover:bg-gray-500">Cancel</button>
          <button onClick={() => onSave(Array.from(selectedRoles))} className="px-4 py-2 text-sm rounded-md bg-netflix-red hover:bg-red-700">Save Roles</button>
        </div>
      </div>
    </div>
  );
};


const AdminManageUsersPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const [editingUser, setEditingUser] = useState(null); // For role editing modal
  const ALL_ROLES = ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_UPLOADER']; // Define available roles

  const { currentUser } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (currentUser && (!currentUser.roles || !currentUser.roles.includes('ROLE_ADMIN'))) {
      navigate('/');
    }
  }, [currentUser, navigate]);

  const fetchAdminUsers = useCallback(async (pageNum) => {
    setLoading(true);
    setError('');
    try {
      const response = await AdminService.getAllUsers(pageNum, 10); // 10 users per page
      setUsers(response.data.content);
      setTotalPages(response.data.totalPages);
      setPage(response.data.number);
    } catch (err) {
      console.error("Failed to fetch admin users:", err);
      setError(err.response?.data?.message || "Could not load users.");
    }
    setLoading(false);
  }, []);

  useEffect(() => {
    if (currentUser && currentUser.roles && currentUser.roles.includes('ROLE_ADMIN')) {
        fetchAdminUsers(page);
    }
  }, [currentUser, page, fetchAdminUsers, refreshTrigger]);

  const handleStatusToggle = async (userId, currentStatus) => {
    if (!window.confirm(`Are you sure you want to ${currentStatus ? 'deactivate' : 'activate'} this user?`)) return;
    try {
      await AdminService.updateUserActiveStatus(userId, !currentStatus);
      setRefreshTrigger(prev => prev + 1);
      alert(`User status updated successfully!`);
    } catch (err) {
      console.error("Failed to update user status:", err);
      alert("Failed to update status: " + (err.response?.data?.message || err.message));
    }
  };
  
  const handleOpenRoleEditor = (user) => {
    setEditingUser(user);
  };

  const handleSaveRoles = async (userId, newRoles) => {
    try {
      await AdminService.updateUserRoles(userId, newRoles);
      setEditingUser(null);
      setRefreshTrigger(prev => prev + 1);
      alert("User roles updated successfully!");
    } catch (err) {
      console.error("Failed to update user roles:", err);
      alert("Failed to update roles: " + (err.response?.data?.message || err.message));
    }
  };

  const renderPagination = () => { /* ... same as in AdminManageVideosPage ... */ };

  if (!currentUser || !currentUser.roles || !currentUser.roles.includes('ROLE_ADMIN')) {
    return <div className="min-h-screen bg-gray-900 flex justify-center items-center text-white"><p>Access Denied.</p></div>;
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <Header />
      <main className="container mx-auto px-4 sm:px-6 py-8">
        <h1 className="text-2xl sm:text-3xl font-bold mb-6">Manage Users</h1>

        {loading && <p className="text-center text-gray-400">Loading users...</p>}
        {error && <p className="text-center text-red-500 bg-red-900 border border-red-700 p-3 rounded-md">{error}</p>}

        {!loading && !error && users.length === 0 && (
          <p className="text-center text-gray-400">No users found.</p>
        )}

        {!loading && !error && users.length > 0 && (
          <div className="overflow-x-auto bg-gray-800 shadow-md rounded-lg">
            <table className="min-w-full divide-y divide-gray-700">
              <thead className="bg-gray-700">
                <tr>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">ID</th>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Name</th>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Email</th>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Roles</th>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Status</th>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Registered</th>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-gray-800 divide-y divide-gray-700">
                {users.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-700/50">
                    <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-400">{user.id}</td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm font-medium text-white">{user.firstName} {user.lastName}</td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-400">{user.email}</td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-400">
                      {user.roles?.join(', ').replace(/ROLE_/g, '') || 'N/A'}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm">
                      <button
                        onClick={() => handleStatusToggle(user.id, user.active)}
                        className={`px-2 py-1 text-xs rounded-full ${
                          user.active ? 'bg-green-600 text-green-100 hover:bg-green-700' : 'bg-red-600 text-red-100 hover:bg-red-700'
                        }`}
                      >
                        {user.active ? 'Active' : 'Inactive'}
                      </button>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-400">{formatDate(user.createdAt)}</td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm font-medium space-x-2">
                      <button onClick={() => handleOpenRoleEditor(user)} className="text-blue-400 hover:text-blue-300">Edit Roles</button>
                      {/* <button className="text-yellow-400 hover:text-yellow-300">View</button> */}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {!loading && renderPagination()}
      </main>

      {editingUser && (
        <EditRolesModal
          user={editingUser}
          availableRoles={ALL_ROLES}
          onSave={(newRoles) => handleSaveRoles(editingUser.id, newRoles)}
          onCancel={() => setEditingUser(null)}
        />
      )}
    </div>
  );
};

export default AdminManageUsersPage;