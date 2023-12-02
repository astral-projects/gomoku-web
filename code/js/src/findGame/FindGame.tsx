import * as React from "react";
import { Navigate } from 'react-router-dom';


type FindGameState =
    | { tag: 'searching' }
    | { tag: 'found' }
    | { tag: 'error'; message: string }
    | { tag: 'in-lobby' }
    | { tag: 'in-game' };

type FindGameAction =
    | { type: 'find' }
    | { type: 'found' }
    | { type: 'error'; message: string }
    | { type: 'join-lobby' }
    | { type: 'start-game' };

function findGameReducer(state: FindGameState, action: FindGameAction): FindGameState {
    switch (action.type) {
        case 'find':
            return { tag: 'searching' };
        case 'found':
            return { tag: 'found' };
        case 'error':
            return { tag: 'error', message: action.message };
        case 'join-lobby':
            return { tag: 'in-lobby' };
        case 'start-game':
            return { tag: 'in-game' };
        default:
            return state; // Retornar o estado atual para qualquer outra ação não tratada
    }
}

export function FindGame() {
    const [state, dispatch] = React.useReducer(findGameReducer, { tag: 'searching' });

    function delay(delayInMs: number) {
        return new Promise(resolve => setTimeout(resolve, delayInMs));
    }

    // Efeito para simular a busca de um jogo e a transição para o estado 'in-lobby'
    React.useEffect(() => {
        delay(5000).then(() => {
            // Suponha que o jogo foi encontrado, agora mudamos para o lobby
            dispatch({ type: 'join-lobby' });
        });
    }, []); // Array de dependências vazio significa que este efeito rodará apenas uma vez após o componente montar

    // Efeito para iniciar o jogo automaticamente quando estiver no estado 'in-lobby'
    React.useEffect(() => {
        let isCancelled = false;

        if (state.tag === 'in-lobby') {
            delay(5000).then(() => {
                if (!isCancelled) {
                    dispatch({ type: 'start-game' });
                }
            });
        }

        // Função de limpeza que define a flag de cancelamento
        return () => {
            isCancelled = true;
        };
    }, [state.tag]);

    const leaveLobby = () => {
        // Pode fazer qualquer limpeza necessária aqui
        dispatch({ type: 'error', message: 'Left the lobby' });
    };

    switch (state.tag) {
        case 'searching':
            return (<div>Searching for a game...</div>);

        case 'in-lobby':
            return (
                <div>
                    In lobby...
                    <button onClick={leaveLobby}>Leave Lobby</button>
                </div>
            );
        case 'in-game': {
            const gameId = 'algumIdDeJogo';
            return <Navigate to={`/game/${gameId}`} replace />;
        }

        case 'error':
            return (
                <div>
                    {state.message} <button onClick={() => dispatch({ type: 'find' })}>Try again</button>
                </div>
            );

        default:
            return (<div>Unexpected state</div>);
    }
}

