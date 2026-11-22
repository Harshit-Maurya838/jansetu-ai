import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useTranslation } from 'react-i18next';
import type { ModuleKey } from '../constants/roles';
import { useAuthStore } from '../store/authStore';
import LoginScreen from '../screens/auth/LoginScreen';
import HomeScreen from '../screens/home/HomeScreen';
import ModulePlaceholderScreen from '../screens/home/ModulePlaceholderScreen';

export type RootStackParamList = {
  Login: undefined;
  Home: undefined;
  Module: { module: ModuleKey };
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function RootNavigator() {
  const user = useAuthStore(s => s.user);
  const { t } = useTranslation();
  return (
    <NavigationContainer>
      <Stack.Navigator>
        {user ? (
          <>
            <Stack.Screen name="Home" component={HomeScreen} options={{ title: t('appName') }} />
            <Stack.Screen
              name="Module"
              component={ModulePlaceholderScreen}
              options={({ route }) => ({ title: t(`modules.${route.params.module}`) })}
            />
          </>
        ) : (
          <Stack.Screen name="Login" component={LoginScreen} options={{ headerShown: false }} />
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}
