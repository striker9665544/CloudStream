// src/pages/PaymentPage.jsx
// import apiClient from "../services/api";
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../services/api'; // Assuming you created api.js with Axios instance
import { useAuth } from '../contexts/AuthContext';

const PaymentPage = () => {
  // In a real app, you'd fetch plans. For now, let's assume we have one.
  // const [plans, setPlans] = useState([]);
  const [selectedPlanId, setSelectedPlanId] = useState(1); // Assume plan ID 1 (Basic Monthly)
  const [cardNumber, setCardNumber] = useState('');
  const [expiryMonth, setExpiryMonth] = useState('');
  const [expiryYear, setExpiryYear] = useState('');
  const [cvv, setCvv] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  // const apiClient = "some other value";

  // useEffect(() => {
  //   // Fetch subscription plans if you have an endpoint for it
  //   apiClient.get('/payments/plans')
  //     .then(response => setPlans(response.data))
  //     .catch(err => setError("Could not load subscription plans."));
  // }, []);


  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!currentUser) {
        setError("You must be logged in to make a payment.");
        return;
    }
    setError('');
    setSuccess('');
    setLoading(true);

    const paymentData = {
      planId: selectedPlanId,
      cardNumber,
      expiryMonth,
      expiryYear,
      cvv,
    };

    try {
      const response = await apiClient.post('/payments/test-transaction', paymentData);
      setSuccess(response.data.message || "Payment successful!");
      // Optionally redirect or update UI
      setTimeout(() => navigate('/'), 2000); // Redirect to home
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Payment processing failed.');
    }
    setLoading(false);
  };

  // Basic validation for card number format
  const handleCardNumberChange = (e) => {
    const value = e.target.value.replace(/\D/g, ''); // Remove non-digits
    if (value.length <= 16) {
      setCardNumber(value);
    }
  };


  return (
    <div className="min-h-screen bg-gray-800 py-12 px-4 sm:px-6 lg:px-8 text-white">
      <div className="max-w-lg mx-auto bg-gray-700 p-8 rounded-lg shadow-xl">
        <h2 className="text-3xl font-extrabold text-center text-white mb-8">Subscribe to CloudCineStream</h2>
        
        {/* Plan Selection (simplified for now) */}
        <div className="mb-6">
            <label className="block text-sm font-medium text-gray-300">Selected Plan</label>
            <p className="text-lg font-semibold">Basic Monthly - $9.99</p> {/* Hardcoded for demo */}
            {/* If you fetch plans:
            <select
                value={selectedPlanId}
                onChange={(e) => setSelectedPlanId(Number(e.target.value))}
                className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-600 focus:outline-none focus:ring-red-500 focus:border-red-500 sm:text-sm rounded-md bg-gray-600"
            >
                {plans.map(plan => (
                    <option key={plan.id} value={plan.id}>{plan.name} - ${plan.price}</option>
                ))}
            </select>
            */}
        </div>

        {error && <p className="bg-red-500 text-white p-3 rounded mb-4 text-sm text-center">{error}</p>}
        {success && <p className="bg-green-500 text-white p-3 rounded mb-4 text-sm text-center">{success}</p>}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="cardNumber" className="block text-sm font-medium text-gray-300">Card Number</label>
            <input
              type="text"
              id="cardNumber"
              value={cardNumber}
              onChange={handleCardNumberChange}
              maxLength="16"
              required
              className="mt-1 appearance-none block w-full px-3 py-2 border border-gray-600 rounded-md shadow-sm placeholder-gray-500 focus:outline-none focus:ring-red-500 focus:border-red-500 sm:text-sm bg-gray-600"
              placeholder="0000 0000 0000 0000"
            />
          </div>

          <div className="grid grid-cols-3 gap-6">
            <div>
              <label htmlFor="expiryMonth" className="block text-sm font-medium text-gray-300">Expiry Month</label>
              <input
                type="text"
                id="expiryMonth"
                value={expiryMonth}
                onChange={(e) => setExpiryMonth(e.target.value.replace(/\D/g, '').slice(0,2))}
                maxLength="2"
                required
                className="mt-1 block w-full px-3 py-2 border border-gray-600 rounded-md shadow-sm placeholder-gray-500 focus:outline-none focus:ring-red-500 focus:border-red-500 sm:text-sm bg-gray-600"
                placeholder="MM"
              />
            </div>
            <div>
              <label htmlFor="expiryYear" className="block text-sm font-medium text-gray-300">Expiry Year</label>
              <input
                type="text"
                id="expiryYear"
                value={expiryYear}
                onChange={(e) => setExpiryYear(e.target.value.replace(/\D/g, '').slice(0,4))}
                maxLength="4"
                required
                className="mt-1 block w-full px-3 py-2 border border-gray-600 rounded-md shadow-sm placeholder-gray-500 focus:outline-none focus:ring-red-500 focus:border-red-500 sm:text-sm bg-gray-600"
                placeholder="YYYY"
              />
            </div>
            <div>
              <label htmlFor="cvv" className="block text-sm font-medium text-gray-300">CVV</label>
              <input
                type="text"
                id="cvv"
                value={cvv}
                onChange={(e) => setCvv(e.target.value.replace(/\D/g, '').slice(0,4))}
                maxLength="4"
                required
                className="mt-1 block w-full px-3 py-2 border border-gray-600 rounded-md shadow-sm placeholder-gray-500 focus:outline-none focus:ring-red-500 focus:border-red-500 sm:text-sm bg-gray-600"
                placeholder="123"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-netflix-red hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:opacity-50"
          >
            {loading ? 'Processing...' : `Pay $9.99 and Subscribe`}
          </button>
        </form>
      </div>
    </div>
  );
};

export default PaymentPage;