// src/services/payment.service.js
import apiClient from './api'; // Your configured Axios instance

const processTestPayment = (paymentData) => {
  return apiClient.post('/payments/test-transaction', paymentData);
};

const PaymentService = {
  processTestPayment,
  // getSubscriptionPlans, // If you add an endpoint for this
};

export default PaymentService;