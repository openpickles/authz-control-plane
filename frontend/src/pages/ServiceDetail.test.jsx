import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ServiceDetail from './ServiceDetail';
import { policyService, policyBundleService, policyBindingService } from '../services/api';

// Mock API
vi.mock('../services/api', () => ({
    default: {},
    policyService: {
        getAll: vi.fn(),
        createCustom: vi.fn(),
    },
    policyBundleService: {
        getAll: vi.fn(),
    },
    policyBindingService: {
        getAll: vi.fn(),
    }
}));

// Mock SlideOver if complex, but simple rendering is fine.
// Verify SlideOver content is rendered when open.

describe('ServiceDetail', () => {
    const mockService = 'test-service';

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders service details after fetching', async () => {
        policyService.getAll.mockResolvedValue({ data: { content: [{ id: 1, name: 'policy1', origin: 'PRODUCT', status: 'ACTIVE' }] } });
        policyBundleService.getAll.mockResolvedValue({ data: { content: [{ id: 1, name: 'bundle1', origin: 'PRODUCT' }] } });
        policyBindingService.getAll.mockResolvedValue({ data: { content: [] } });

        render(
            <MemoryRouter initialEntries={[`/services/${mockService}`]}>
                <Routes>
                    <Route path="/services/:name" element={<ServiceDetail />} />
                </Routes>
            </MemoryRouter>
        );

        expect(screen.getByText('Loading...')).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText(`Service: ${mockService}`)).toBeInTheDocument();
        });

        expect(screen.getByText('policy1')).toBeInTheDocument();
        expect(screen.getByText('bundle1')).toBeInTheDocument();
        expect(screen.getByText('PRODUCT')).toBeVisible();
    });

    it('opens customization modal and saves override', async () => {
        const policy = { id: 1, name: 'policy1', origin: 'PRODUCT', content: 'package test', version: 'v1' };
        policyService.getAll.mockResolvedValue({ data: { content: [policy] } });
        policyBundleService.getAll.mockResolvedValue({ data: { content: [] } });
        policyBindingService.getAll.mockResolvedValue({ data: { content: [] } });
        policyService.createCustom.mockResolvedValue({});

        render(
            <MemoryRouter initialEntries={[`/services/${mockService}`]}>
                <Routes>
                    <Route path="/services/:name" element={<ServiceDetail />} />
                </Routes>
            </MemoryRouter>
        );

        await waitFor(() => screen.getByText('policy1'));

        // Click Customize
        fireEvent.click(screen.getByText('Customize / Override'));

        // Check modal open
        expect(screen.getByText('Customize: policy1')).toBeInTheDocument();

        // Edit content
        const textarea = screen.getByRole('textbox');
        fireEvent.change(textarea, { target: { value: 'package custom' } });

        // Save
        fireEvent.click(screen.getByText('Save Override'));

        await waitFor(() => {
            expect(policyService.createCustom).toHaveBeenCalledWith(expect.objectContaining({
                name: 'policy1',
                serviceOwner: mockService,
                origin: 'CUSTOM',
                content: 'package custom'
            }));
        });
    });
});
