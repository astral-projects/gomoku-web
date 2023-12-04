import * as React from 'react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import './Rankings.css';

interface User {
    id: number;
    name: string;
    score: number;
}

const users: User[] = [
    { id: 1, name: 'Alice', score: 100 },
    { id: 2, name: 'Bob', score: 95 },
    { id: 3, name: 'Charlie', score: 90 },
    { id: 4, name: 'David', score: 85 },
    { id: 5, name: 'Eve', score: 80 },
    { id: 6, name: 'Frank', score: 75 },
    { id: 7, name: 'Grace', score: 70 },
    { id: 8, name: 'Heidi', score: 65 },
    { id: 9, name: 'Ivan', score: 60 },
    { id: 10, name: 'Judy', score: 55 },
    { id: 11, name: 'Mallory', score: 50 },
    { id: 12, name: 'Oscar', score: 45 },
    { id: 13, name: 'Peggy', score: 40 },
    { id: 14, name: 'Rupert', score: 35 },
    { id: 15, name: 'Sybil', score: 30 },
    { id: 16, name: 'Trudy', score: 25 },
    { id: 17, name: 'Victor', score: 20 },
    { id: 18, name: 'Walter', score: 15 },
    { id: 19, name: 'Wendy', score: 10 },
    { id: 20, name: 'Zoe', score: 5 },
];

const ITEMS_PER_PAGE = 5;

type RankingsState =
    | { tag: 'idle'; users: User[]; error?: string }
    | { tag: 'loading' }
    | { tag: 'loaded'; users: User[] }
    | { tag: 'error'; message: string };

type RankingsAction =
    | { type: 'load' }
    | { type: 'success'; users: User[] }
    | { type: 'error'; message: string };

function rankingsReducer(state: RankingsState, action: RankingsAction): RankingsState {
    switch (state.tag) {
        case 'idle':
        case 'loaded':
            if (action.type === 'load') {
                return { tag: 'loading' };
            }
            return state;

        case 'loading':
            if (action.type === 'success') {
                return { tag: 'loaded', users: action.users };
            } else if (action.type === 'error') {
                return { tag: 'error', message: action.message };
            }
            return state;

        case 'error':
            if (action.type === 'load') {
                return { tag: 'loading' };
            }
            return state;

        default:
            return state;
    }
}

function delay(delayInMs: number) {
    return new Promise(resolve => {
        setTimeout(() => resolve(undefined), delayInMs);
    });
}

async function simulateApiCall(): Promise<User[]> {
    await delay(1000);
    return users;
}


export function Rankings() {
    const [searchTerm, setSearchTerm] = useState('');
    const [currentPage, setCurrentPage] = useState(0);
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [state, dispatch] = React.useReducer(rankingsReducer, { tag: 'idle', users: [] });

    React.useEffect(() => {
        dispatch({ type: 'load' });
        simulateApiCall()
            .then(users => {
                dispatch({ type: 'success', users });
            })
            .catch(error => {
                dispatch({ type: 'error', message: error.message || 'An unexpected error occurred' });
            });
    }, []);

    const filteredUsers = users.filter(user =>
        user.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const totalPages = Math.ceil(filteredUsers.length / ITEMS_PER_PAGE) || 1;

    const handleUserClick = (user: User) => {
        setSelectedUser(user);
    };

    const goToPrevPage = () => {
        setCurrentPage((prev) => (prev > 0 ? prev - 1 : 0));
    };

    const goToNextPage = () => {
        setCurrentPage((prev) => (prev < totalPages - 1 ? prev + 1 : totalPages - 1));
    };

    const getCurrentPageItems = () => {
        const start = currentPage * ITEMS_PER_PAGE;
        return filteredUsers.slice(start, start + ITEMS_PER_PAGE);
    };
    
    if (selectedUser) {
        return (
          <div>
            <h2>Statistics for {selectedUser.name}</h2>
            <p>Score: {selectedUser.score}</p>
            <button onClick={() => setSelectedUser(null)}>Back to Leaderboard</button>
          </div>
        );
      }

    switch (state.tag) {
        case 'idle':
        case 'loaded':
            return (
                <div>
                    <h1>Leaderboard</h1>
                    <input
                        type="text"
                        placeholder="Search User"
                        value={searchTerm}
                        onChange={(e) => {
                            setSearchTerm(e.target.value);
                            setCurrentPage(0);
                        }}
                    />
                    <div>
                        {getCurrentPageItems().map(user => (
                            <div key={user.id} onClick={() => handleUserClick(user)} style={{ cursor: 'pointer' }}>
                                {`${user.name}: ${user.score}`}
                            </div>
                        ))}
                    </div>
                    {totalPages > 1 && (
                        <div className="pagination-controls">
                            <button onClick={goToPrevPage} disabled={currentPage === 0}>
                                Prev
                            </button>
                            <button onClick={goToNextPage} disabled={currentPage === totalPages - 1}>
                                Next
                            </button>
                        </div>
                    )}
                    <p className="scroll-instruction">Page {currentPage + 1} of {totalPages}</p>
                    <p>
                        <Link to="/">
                            <button>Back to Home</button>
                        </Link>
                    </p>
                </div>
            );
            

        case 'loading':
            return <div>Loading...</div>;

        case 'error':
            return <div>Error: {state.message}</div>;

        default:
            return <div>Unexpected state</div>;
            
    }
}
