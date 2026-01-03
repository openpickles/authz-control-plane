import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import Login from './Login';
import axios from 'axios';
import { BrowserRouter } from 'react-router-dom';

vi.mock('axios');
const mockedAxios = vi.mocked(axios);

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

describe('Login Component', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    test('renders login form', () => {
        render(<BrowserRouter><Login /></BrowserRouter>);
        expect(screen.getByText('Policy Engine')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Enter username')).toBeInTheDocument();
    });

    test('handles successful login', async () => {
        mockedAxios.post.mockResolvedValue({});

        render(<BrowserRouter><Login /></BrowserRouter>);

        fireEvent.change(screen.getByPlaceholderText('Enter username'), { target: { value: 'test' } });
        fireEvent.change(screen.getByPlaceholderText('Enter password'), { target: { value: 'pass' } });
        fireEvent.click(screen.getByText('Sign In'));

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/');
        });
    });

    test('handles 401 error', async () => {
        mockedAxios.post.mockRejectedValue({
            response: { status: 401 }
        });

        render(<BrowserRouter><Login /></BrowserRouter>);

        fireEvent.change(screen.getByPlaceholderText('Enter username'), { target: { value: 'test' } });
        fireEvent.change(screen.getByPlaceholderText('Enter password'), { target: { value: 'wrong' } });
        fireEvent.click(screen.getByText('Sign In'));

        await waitFor(() => {
            expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
        });
    });

    test('handles other errors', async () => {
        mockedAxios.post.mockRejectedValue(new Error('Network Error'));

        render(<BrowserRouter><Login /></BrowserRouter>);

        fireEvent.change(screen.getByPlaceholderText('Enter username'), { target: { value: 'test' } });
        fireEvent.change(screen.getByPlaceholderText('Enter password'), { target: { value: 'pass' } });
        fireEvent.click(screen.getByText('Sign In'));

        await waitFor(() => {
            expect(screen.getByText('Login failed. Please check backend connection.')).toBeInTheDocument();
        });
    });
});
