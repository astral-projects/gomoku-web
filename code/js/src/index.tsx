import * as React from 'react';
import { createRoot } from 'react-dom/client';

import { App } from './App';

const rootElement = createRoot(document.getElementById('main-div'));
rootElement.render(<App />);

