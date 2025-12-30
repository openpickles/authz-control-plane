import axios from 'axios';
const API_URL = '/api/v1/audit-logs';

export const getAuditLogs = async (params) => {
    // Clean params to remove null/undefined/empty string values appropriately if needed,
    // though axios handles null/undefined by not sending them. Empty strings might be sent.
    try {
        const response = await axios.get(API_URL, { params });
        return response.data;
    } catch (error) {
        console.error("Error fetching audit logs", error);
        throw error;
    }
};
