import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import PolicyEditor from './PolicyEditor';
import { policyService } from '../services/api';

// Mock the API service
vi.mock('../services/api', () => ({
    policyService: {
        getAll: vi.fn(),
        create: vi.fn(),
        update: vi.fn(),
        delete: vi.fn(),
    },
}));

describe('PolicyEditor Component', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        // Setup default mock response
        policyService.getAll.mockResolvedValue({ data: [] });
    });

    test('renders Policy Editor correctly', async () => {
        render(<PolicyEditor />);
        expect(screen.getByText('Policies')).toBeInTheDocument();
        expect(screen.getByText('No Policy Selected')).toBeInTheDocument();
    });

    test('allows creating a new policy', async () => {
        render(<PolicyEditor />);

        // Click New Policy button
        const newButton = screen.getByTitle('New Policy');
        fireEvent.click(newButton);

        // Check for form fields
        expect(screen.getByPlaceholderText('Policy Name (ID)')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('filename.rego (unique)')).toBeInTheDocument();

        // Fill form
        fireEvent.change(screen.getByPlaceholderText('Policy Name (ID)'), { target: { value: 'test.policy' } });
        fireEvent.change(screen.getByPlaceholderText('filename.rego (unique)'), { target: { value: 'test.rego' } });
        fireEvent.change(screen.getByPlaceholderText('Short description...'), { target: { value: 'Test Description' } });

        // Save
        policyService.create.mockResolvedValue({
            data: { id: 1, name: 'test.policy', filename: 'test.rego', status: 'DRAFT', version: '1.0' }
        });
        policyService.getAll.mockResolvedValue({
            data: [{ id: 1, name: 'test.policy', filename: 'test.rego', status: 'DRAFT', version: '1.0' }]
        });

        const saveButton = screen.getByText('Save Changes');
        fireEvent.click(saveButton);

        await waitFor(() => {
            expect(policyService.create).toHaveBeenCalledWith(expect.objectContaining({
                name: 'test.policy',
                filename: 'test.rego',
                description: 'Test Description'
            }));
        });
    });

    test('loads existing policies', async () => {
        const mockPolicies = [
            { id: 1, name: 'policy1', filename: 'p1.rego', version: '1.0', status: 'ACTIVE' },
            { id: 2, name: 'policy2', filename: 'p2.rego', version: '1.1', status: 'DRAFT' }
        ];
        policyService.getAll.mockResolvedValue({ data: mockPolicies });

        render(<PolicyEditor />);

        await waitFor(() => {
            expect(screen.getByText('policy1')).toBeInTheDocument();
            expect(screen.getByText('policy2')).toBeInTheDocument();
        });
    });
});
