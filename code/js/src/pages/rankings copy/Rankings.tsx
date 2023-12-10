import * as React from 'react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchUsersStats, fetchUserStatsBySearchTerm } from '../../services/usersServices';
import { UserStats } from '../../domain/UserStats.js';
import { ProblemModel } from '../../services/media/ProblemModel.js';
import { isSuccessful } from '../utils/responseData';
import { PaginatedResult } from '../../services/models/users/PaginatedResultModel.js';
import './Rankings.css';
import { getHrefByRel } from '../../services/media/siren/Link';

type State =
    | { tag: 'loading' }
    | { tag: 'loaded'; data: PaginatedResult<UserStats> }
    | { tag: 'error'; message: string };

type Action =
    | { type: 'load' }
    | { type: 'success'; data: PaginatedResult<UserStats> }
    | { type: 'error'; message: string };

function rankingsReducer(state: State, action: Action): State {
    switch (state.tag) {
        case 'loaded':
            if (action.type === 'load') {
                return { tag: 'loading' };
            }
            return state;

        case 'loading':
            if (action.type === 'success') {
                return { tag: 'loaded', data: action.data };
            } else if (action.type === 'error') {
                return { tag: 'error', message: action.message };
            }
            return state;

        case 'error':
            if (action.type === 'load') {
                return { tag: 'loading' };
            }
            return state;
    }
}

export function Rankings() {
    const [searchTerm, setSearchTerm] = useState('');
    const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
    const [selectedUser, setSelectedUser] = useState<UserStats | null>(null);
    const [state, dispatch] = React.useReducer(rankingsReducer, { tag: 'loading' });

    const fetchPage = (relName: string) => {
        if (state.tag === 'loaded') {
            dispatch({ type: 'load' });
            const href = getHrefByRel(state.data.links, relName);
            fetchUsersStats(href)
                .then(result => {
                    if (!isSuccessful(result.contentType)) {
                        const errorData = result.json as ProblemModel;
                        dispatch({ type: 'error', message: errorData.detail });
                    } else {
                        const successData = result.json as PaginatedResult<UserStats>;
                        dispatch({ type: 'success', data: successData });
                    }
                })
                .catch((err: { message: string }) => {
                    dispatch({ type: 'error', message: err.message });
                });
        }
    };

    React.useEffect(() => {
        if (state.tag === 'loading') {
            dispatch({ type: 'load' });
            fetchUsersStats()
                .then(result => {
                    if (!isSuccessful(result.contentType)) {
                        const errorData = result.json as ProblemModel;
                        dispatch({ type: 'error', message: errorData.detail });
                    } else {
                        const successData = result.json as PaginatedResult<UserStats>;
                        dispatch({ type: 'success', data: successData });
                    }
                })
                .catch((err: { message: string }) => {
                    dispatch({ type: 'error', message: err.message });
                });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    React.useEffect(() => {
        // Set a timer to update debouncedSearchTerm after x milliseconds of no changes
        const timerId = setTimeout(() => {
            if (searchTerm.length >= 4) {
                setDebouncedSearchTerm(searchTerm);
            }
        }, 1000);

        return () => {
            clearTimeout(timerId);
        };
    }, [searchTerm]);

    React.useEffect(() => {
        if (state.tag === 'loaded') {
            dispatch({ type: 'load' });
            fetchUserStatsBySearchTerm(debouncedSearchTerm)
                .then(result => {
                    if (!isSuccessful(result.contentType)) {
                        const errorData = result.json as ProblemModel;
                        console.log(result.json);
                        dispatch({ type: 'error', message: errorData.detail });
                    } else {
                        const successData = result.json as PaginatedResult<UserStats>;
                        dispatch({ type: 'success', data: successData });
                    }
                })
                .catch((err: { message: string }) => {
                    dispatch({ type: 'error', message: err.message });
                });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [debouncedSearchTerm]);

    const handleUserClick = (user: UserStats) => {
        setSelectedUser(user);
    };

    const goToFirstPage = () => {
        fetchPage('first');
    };
    const goToPrevPage = () => {
        fetchPage('prev');
    };
    const goToNextPage = () => {
        fetchPage('next');
    };
    const goToLastPage = () => {
        fetchPage('last');
    };

    if (selectedUser) {
        return (
            <div>
                <h2>Statistics for {selectedUser.username.value}</h2>
                <p>Points: {selectedUser.points.value}</p>
                <button onClick={() => setSelectedUser(null)}>Back to Leaderboard</button>
            </div>
        );
    }

    switch (state.tag) {
        case 'loaded':
            return (
                <div>
                    <h1>Leaderboard</h1>
                    <input
                        type="text"
                        placeholder="Search User"
                        value={searchTerm}
                        onChange={e => {
                            setSearchTerm(e.target.value);
                        }}
                    />
                    <div>
                        <table>
                            <thead>
                                <tr>
                                    <th>Rank</th>
                                    <th>Username</th>
                                    <th>Points</th>
                                </tr>
                            </thead>
                            <tbody>
                                {state.data.properties.items.map(user => (
                                    <tr
                                        key={user.id.value}
                                        onClick={() => handleUserClick(user)}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        <td>{user.rank.value}</td>
                                        <td>{user.username.value}</td>
                                        <td>{user.points.value}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    {state.data.properties.totalPages > 1 && (
                        <div className="pagination-controls">
                            <button onClick={goToFirstPage} disabled={state.data.properties.currentPage === 1}>
                                First
                            </button>
                            <button onClick={goToPrevPage} disabled={state.data.properties.currentPage === 1}>
                                Prev
                            </button>
                            <button
                                onClick={goToNextPage}
                                disabled={state.data.properties.currentPage === state.data.properties.totalPages}
                            >
                                Next
                            </button>
                            <button
                                onClick={goToLastPage}
                                disabled={state.data.properties.currentPage === state.data.properties.totalPages}
                            >
                                Last
                            </button>
                        </div>
                    )}
                    <p className="scroll-instruction">
                        Page {state.data.properties.currentPage} of {state.data.properties.totalPages}
                    </p>
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
    }
}