module.exports = {
  preset: '@react-native/jest-preset',
  setupFiles: ['<rootDir>/jest/setup.ts'],
  // babel-preset-expo rewrites process.env.EXPO_PUBLIC_* into an ESM import from expo/virtual/env, so expo must be transformed too.
  transformIgnorePatterns: ['node_modules/(?!((jest-)?react-native|@react-native(-community)?|expo(-.*)?)/)'],
};
