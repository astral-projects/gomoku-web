import * as React from 'react';
import {Link, useLocation} from 'react-router-dom';
import {fetchUserStatsByUserId} from '../../services/userServices';
import {UserStats} from '../../domain/UserStats.js';
import {ProblemModel} from '../../services/media/ProblemModel.js';
import {isSuccessful} from '../utils/responseData';
import {UserStatsOutput} from '../../services/models/users/UserStatsOutputModel';

type State = { tag: 'loading' } | { tag: 'loaded'; data: UserStats } | { tag: 'error'; message: string };

type Action = { type: 'load' } | { type: 'success'; data: UserStats } | { type: 'error'; message: string };

function reducer(state: State, action: Action): State {
    switch (state.tag) {
        case 'loaded':
            if (action.type === 'load') {
                return {tag: 'loading'};
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

export function UserStats() {
    const [state, dispatch] = React.useReducer(reducer, {tag: 'loading'});
    const location = useLocation();
    const userId = location.pathname.split('/')[2];

    React.useEffect(() => {
        dispatch({type: 'load'});
        fetchUserStatsByUserId(userId)
            .then(result => {
                console.log('result: ' + JSON.stringify(result));
                if (!isSuccessful(result.contentType)) {
                    const errorData = result.json as ProblemModel;
                    dispatch({type: 'error', message: errorData.detail});
                } else {
                    const successData = result.json as UserStatsOutput;
                    const properties = successData.properties as UserStats;
                    dispatch({type: 'success', data: properties});
                }
            })
            .catch((err: { message: string }) => {
                dispatch({type: 'error', message: err.message});
            });
    }, [userId]);

    switch (state.tag) {
        case 'loaded':
            return (
                <div>
                    <div>
                        <h2>UserStats</h2>
                        <table>
                            <tbody>
                            <tr>
                                <td>Rank</td>
                                <td>{state.data.rank.value}</td>
                            </tr>
                            <tr>
                                <td>Username</td>
                                <td>{state.data.username.value}</td>
                            </tr>
                            <tr>
                                <td>Points</td>
                                <td>{state.data.points.value}</td>
                            </tr>
                            <tr>
                                <td>Games Played</td>
                                <td>{state.data.gamesPlayed.value}</td>
                            </tr>
                            <tr>
                                <td>Games Won</td>
                                <td>{state.data.wins.value}</td>
                            </tr>
                            <tr>
                                <td>Games Lost</td>
                                <td>{state.data.losses.value}</td>
                            </tr>
                            <tr>
                                <td>Games Drawn</td>
                                <td>{state.data.draws.value}</td>
                            </tr>
                            </tbody>
                        </table>
                        <Link to="/rankings">
                            <button>Back to Rankings</button>
                        </Link>
                    </div>
                </div>
            );

        case 'loading':
            return <div>Loading...</div>;

        case 'error':
            return <div>Error: {state.message}</div>;
    }
}
