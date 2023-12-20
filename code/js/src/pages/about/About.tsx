import * as React from 'react';
import {useReducer} from 'react';
import {ProblemModel} from '../../services/media/ProblemModel.js';
import {isSuccessful} from '../utils/responseData';
import {fetchSystemInfo} from '../../services/systemService';
import {SystemOutput, SystemOutputModel} from '../../services/models/system/SystemOutputModel';

type State = { tag: 'loading' } | { tag: 'loaded'; data: SystemOutputModel } | { tag: 'error'; message: string };

type Action = { type: 'load' } | { type: 'success'; data: SystemOutputModel } | { type: 'error'; message: string };

function reducer(state: State, action: Action): State {
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

export function About() {
    const [state, dispatch] = useReducer(reducer, { tag: 'loading' });

    React.useEffect(() => {
        dispatch({ type: 'load' });
        fetchSystemInfo()
            .then(result => {
                if (!isSuccessful(result.contentType)) {
                    const errorData = result.json as ProblemModel;
                    dispatch({ type: 'error', message: errorData.detail });
                } else {
                    const successData = result.json as SystemOutput;
                    const properties = successData.properties as SystemOutputModel;
                    dispatch({ type: 'success', data: properties });
                }
            })
            .catch((err: { message: string }) => {
                dispatch({ type: 'error', message: err.message });
            });
    }, []);

    switch (state.tag) {
        case 'loaded':
            return (
                <div>
                    <h2>{state.data.gameName}</h2>
                    <p>Version: {state.data.version}</p>
                    <p>Description: {state.data.description}</p>
                    <p>Release Date: {state.data.releaseDate}</p>

                    <h3>Authors:</h3>
                    <ul>
                        {state.data.authors.map((author, index) => (
                            <li key={index}>
                                {author.firstName} {author.lastName} - GitHub:{' '}
                                <a href={author.gitHubUrl}>{author.gitHubUrl}</a>
                            </li>
                        ))}
                    </ul>
                </div>
            );

        case 'loading':
            return <div>Loading...</div>;

        case 'error':
            return <div>Error: {state.message}</div>;
    }
}
