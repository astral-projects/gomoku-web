type State = {
    tag: string;
};

type Action = {
    type: string;
};

export function logUnexpectedAction(state: State, action: Action) {
  console.log(`Unexpected action '${action.type} on state '${state.tag}'`);
}
