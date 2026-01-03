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
        expect(screen.getByText('Explorer')).toBeInTheDocument();
        expect(screen.getByText('No policies found.')).toBeInTheDocument();
    });

    test('allows creating a new policy', async () => {
        render(<PolicyEditor />);

        // Click New Policy button
        const newButton = screen.getByTitle('New Policy');
        fireEvent.click(newButton);

        // Check for form fields
        expect(screen.getByPlaceholderText('Policy Name')).toBeInTheDocument();

        // Open Settings to see filename
        const configButton = screen.getByText('Config');
        fireEvent.click(configButton);
        expect(screen.getByPlaceholderText('policy.rego')).toBeInTheDocument();

        // Fill form
        fireEvent.change(screen.getByPlaceholderText('Policy Name'), { target: { value: 'test.policy' } });
        fireEvent.change(screen.getByPlaceholderText('policy.rego'), { target: { value: 'test.rego' } });

        // Close Settings
        const doneButton = screen.getByText('Done');
        fireEvent.click(doneButton);

        // Save
        policyService.create.mockResolvedValue({
            data: { id: 1, name: 'test.policy', filename: 'test.rego', status: 'DRAFT', version: '1.0' }
        });
        policyService.getAll.mockResolvedValue({
            data: [{ id: 1, name: 'test.policy', filename: 'test.rego', status: 'DRAFT', version: '1.0' }]
        });

        const saveButton = screen.getByTitle('Save Policy (Ctrl+S)');
        fireEvent.click(saveButton);

        await waitFor(() => {
            expect(policyService.create).toHaveBeenCalledWith(expect.objectContaining({
                name: 'test.policy',
                filename: 'test.rego',
                description: ''
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
