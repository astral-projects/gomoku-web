import * as React from 'react';
import {useEffect, useReducer, useState} from 'react';
import {Link, Navigate} from 'react-router-dom';
import {fetchUsersStats, fetchUserStatsBySearchTerm} from '../../services/usersService';
import {UserStats} from '../../domain/UserStats.js';
import {ProblemModel} from '../../services/media/ProblemModel.js';
import {isSuccessful} from '../utils/responseData';
import {PaginatedResult} from '../../services/models/users/PaginatedResultModel.js';
import './Rankings.css';
import {getHrefByRel} from '../../services/media/siren/Link';
import {replacePathVariables} from '../utils/replacePathVariables';
import {webRoutes} from '../../App';

const minSearchTermLength = 4;

type State =
    | { tag: 'loading' }
    | { tag: 'redirecting'; user: UserStats }
    | { tag: 'loaded'; data: PaginatedResult<UserStats> }
    | { tag: 'error'; message: string };

type Action =
    | { type: 'load' }
    | { type: 'rowClick'; user: UserStats }
    | { type: 'success'; data: PaginatedResult<UserStats> }
    | { type: 'error'; message: string };

function rankingsReducer(state: State, action: Action): State {
    switch (state.tag) {
        case 'loaded':
            if (action.type === 'load') {
                return {tag: 'loading'};
            } else if (action.type === 'rowClick') {
                return {tag: 'redirecting', user: action.user};
            }
            return state;

        case 'loading':
            if (action.type === 'success') {
                return {tag: 'loaded', data: action.data};
            } else if (action.type === 'error') {
                return {tag: 'error', message: action.message};
            }
            return state;

        case 'error':
            if (action.type === 'load') {
                return {tag: 'loading'};
            }
            return state;
    }
}

export function Rankings() {
    const [searchTerm, setSearchTerm] = useState('');
    const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
    const [state, dispatch] = useReducer(rankingsReducer, {tag: 'loading'});

    useEffect(() => {
        fetchUserStatsFirstPage()
    }, []);

    const fetchUserStatsFirstPage = () => {
        dispatch({type: 'load'})
        fetchUsersStats()
            .then(result => {
                if (!isSuccessful(result.contentType)) {
                    const errorData = result.json as ProblemModel;
                    dispatch({type: 'error', message: errorData.detail});
                } else {
                    const successData = result.json as PaginatedResult<UserStats>;
                    dispatch({type: 'success', data: successData});
                }
            })
            .catch((err: { message: string }) => {
                dispatch({type: 'error', message: err.message});
            });
    }

    const fetchPage = (relName: string) => {
        if (state.tag === 'loaded') {
            dispatch({type: 'load'});
            const href = getHrefByRel(state.data.links, relName);
            fetchUsersStats(href)
                .then(result => {
                    if (!isSuccessful(result.contentType)) {
                        const errorData = result.json as ProblemModel;
                        dispatch({type: 'error', message: errorData.detail});
                    } else {
                        const successData = result.json as PaginatedResult<UserStats>;
                        dispatch({type: 'success', data: successData});
                    }
                })
                .catch((err: { message: string }) => {
                    dispatch({type: 'error', message: err.message});
                });
        }
    };

    useEffect(() => {
        if (state.tag === 'loaded') {
            // Set a timer to update debouncedSearchTerm after x milliseconds of no changes
            const timerId = setTimeout(() => {
                if (searchTerm.length >= minSearchTermLength) {
                    setDebouncedSearchTerm(searchTerm);
                } else if (searchTerm.length === 0) {
                    // cannot call the first page of the current data representation,
                    // which in this case is the ranking leaderboard and not the search results
                    fetchUserStatsFirstPage();
                }
            }, 1000);

            return () => {
                clearTimeout(timerId);
            };
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [searchTerm]);

    useEffect(() => {
        if (state.tag === 'loaded') {
            dispatch({type: 'load'});
            fetchUserStatsBySearchTerm(debouncedSearchTerm)
                .then(result => {
                    if (!isSuccessful(result.contentType)) {
                        const errorData = result.json as ProblemModel;
                        dispatch({type: 'error', message: errorData.detail});
                    } else {
                        const successData = result.json as PaginatedResult<UserStats>;
                        dispatch({type: 'success', data: successData});
                    }
                })
                .catch((err: { message: string }) => {
                    dispatch({type: 'error', message: err.message});
                });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [debouncedSearchTerm]);

    const handleClearSearch = () => {
        setSearchTerm('');
    };

    const handleUserClick = (user: UserStats) => {
        dispatch({type: 'rowClick', user: user});
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

    switch (state.tag) {
        case 'redirecting': {
            const userId = state.user.id.value;
            return <Navigate to={replacePathVariables(webRoutes.userStats, [userId])} replace={true}/>;
        }
        case 'loaded':
            return (
                <div>
                    <h1>Leaderboard</h1>
                    <div>
                        <input
                            type="text"
                            placeholder="Search Username"
                            value={searchTerm}
                            onChange={e => {
                                setSearchTerm(e.target.value);
                            }}
                        />
                        <button onClick={handleClearSearch}>Clear</button>
                    </div>
                    {state.tag === 'loaded' && state.data.properties.items.length === 0 ? (
                            <p>We searched far and wide, but could not find any users matching your search term.</p>
                        ) :
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
                                        style={{cursor: 'pointer'}}
                                    >
                                        <td>{user.rank.value}</td>
                                        <td>{user.username.value}</td>
                                        <td>{user.points.value}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    }
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
                    )
                    }
                    <p className="scroll-instruction">
                        Page {state.data.properties.currentPage} of {state.data.properties.totalPages}
                    </p>
                    <p>
                        <Link to={webRoutes.home}>
                            <button>Back to Home</button>
                        </Link>
                    </p>
                </div>
            )
        case 'loading':
            return <div>Loading...</div>;
        case 'error':
            return <div>Error: {state.message}</div>;
    }
}
