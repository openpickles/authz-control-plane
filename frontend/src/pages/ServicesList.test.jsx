import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
import ServicesList from './ServicesList';
import { serviceRegistryService } from '../services/api';

// Mock the API module
vi.mock('../services/api', () => ({
    default: {},
    serviceRegistryService: {
        getAll: vi.fn(),
    },
}));

describe('ServicesList', () => {
    it('renders loading state initially', () => {
        serviceRegistryService.getAll.mockImplementation(() => new Promise(() => { })); // Never resolving promise
        render(<BrowserRouter><ServicesList /></BrowserRouter>);
        expect(screen.getByText('Loading services...')).toBeInTheDocument();
    });

    it('renders services list after data fetch', async () => {
        const mockServices = [
            { id: 1, name: 'service-a', version: '1.0', description: 'Service A', lastSyncTime: '2023-01-01T10:00:00Z' },
            { id: 2, name: 'service-b', version: '2.0', description: 'Service B', lastSyncTime: null },
        ];
        serviceRegistryService.getAll.mockResolvedValue({ data: mockServices });

        render(<BrowserRouter><ServicesList /></BrowserRouter>);

        await waitFor(() => {
            expect(screen.getByText('service-a')).toBeInTheDocument();
            expect(screen.getByText('service-b')).toBeInTheDocument();
        });

        expect(screen.getByText('Service A')).toBeInTheDocument();
        expect(screen.getByText('Service B')).toBeInTheDocument();
        expect(screen.getByText('Never')).toBeInTheDocument(); // For lastSyncTime null
    });

    it('renders error state on fetch failure', async () => {
        serviceRegistryService.getAll.mockRejectedValue(new Error('Fetch failed'));

        render(<BrowserRouter><ServicesList /></BrowserRouter>);

        await waitFor(() => {
            expect(screen.getByText('Error: Fetch failed')).toBeInTheDocument();
        });
    });
});
