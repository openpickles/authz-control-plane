import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import PolicyBindings from './PolicyBindings';
import { policyBindingService, resourceTypeService, policyService } from '../services/api';

// Mock APIs
vi.mock('../services/api', () => ({
    policyBindingService: {
        getAll: vi.fn(),
        create: vi.fn(),
        delete: vi.fn(),
    },
    resourceTypeService: {
        getAll: vi.fn(),
    },
    policyService: {
        getAll: vi.fn(),
    }
}));

describe('PolicyBindings Component', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        policyBindingService.getAll.mockResolvedValue({ data: [] });
        resourceTypeService.getAll.mockResolvedValue({ data: [] });
        policyService.getAll.mockResolvedValue({ data: [] });
    });

    test('renders Policy Bindings page', async () => {
        render(<PolicyBindings />);
        expect(screen.getByText('Policy Bindings')).toBeInTheDocument();
        expect(screen.getByText('Bind policies to specific resource types and execution contexts.')).toBeInTheDocument();
    });

    test('opens modal and allows creating binding with multiple policies', async () => {
        const user = userEvent.setup();

        // Mock data
        const mockPolicies = [
            { id: 1, name: 'authz', filename: 'authz.rego' },
            { id: 2, name: 'admin', filename: 'admin.rego' }
        ];
        const mockTypes = [
            { id: 1, key: 'DOCUMENT', name: 'Document Service' }
        ];

        policyService.getAll.mockResolvedValue({ data: { content: mockPolicies } });
        resourceTypeService.getAll.mockResolvedValue({ data: { content: mockTypes } });

        render(<PolicyBindings />);

        // Open modal
        const newButton = screen.getByText('Create Binding');
        await user.click(newButton);

        await waitFor(() => {
            expect(screen.getByText('Create Policy Binding')).toBeInTheDocument();
        });

        // Fill form
        // wait for options to populate
        await waitFor(() => {
            expect(screen.getByRole('combobox', { name: /resource type/i })).toHaveTextContent('Document Service');
        });

        await user.selectOptions(screen.getByRole('combobox', { name: /resource type/i }), 'DOCUMENT');
        await user.type(screen.getByPlaceholderText('e.g., fine_grained_access'), 'access');

        // Select multiple policies
        // Click the trigger to open MultiSelect
        const multiSelectTrigger = screen.getByText('Select policies...');
        await user.click(multiSelectTrigger);

        // Click options
        const option1 = screen.getByText(/authz/i);
        await user.click(option1);
        const option2 = screen.getByText(/admin/i);
        await user.click(option2);

        // Click outside or just submit (options are selected on click)

        // Create
        const createButton = screen.getByText('Create Binding'); // The submit button
        await user.click(createButton);

        await waitFor(() => {
            expect(policyBindingService.create).toHaveBeenCalledWith(expect.objectContaining({
                resourceType: 'DOCUMENT',
                context: 'access',
                policyIds: [1, 2] // Expect IDs now
            }));
        });
    });
});
