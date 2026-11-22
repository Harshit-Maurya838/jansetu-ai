/**
 * @format
 */

import 'react-native-get-random-values'; // must run before any uuid generation
import { AppRegistry } from 'react-native';
import App from './App';
import { name as appName } from './app.json';

AppRegistry.registerComponent(appName, () => App);
