import React, { useEffect } from 'react';
import { ActivityIndicator, AppState, Pressable, StyleSheet, Text, View } from 'react-native';
import { NavigationContainer, useNavigation } from '@react-navigation/native';
import { createNativeStackNavigator, type NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useTranslation } from 'react-i18next';
import { colors } from '../components/theme';
import ActivateScreen from '../screens/auth/ActivateScreen';
import ForgotPasswordScreen from '../screens/auth/ForgotPasswordScreen';
import LoginScreen from '../screens/auth/LoginScreen';
import OtpVerifyScreen from '../screens/auth/OtpVerifyScreen';
import PinUnlockScreen from '../screens/auth/PinUnlockScreen';
import ResetPasswordScreen from '../screens/auth/ResetPasswordScreen';
import SetPinScreen from '../screens/auth/SetPinScreen';
import SignUpScreen from '../screens/auth/SignUpScreen';
import WelcomeScreen from '../screens/auth/WelcomeScreen';
import HomeScreen from '../screens/home/HomeScreen';
import ModulePlaceholderScreen from '../screens/home/ModulePlaceholderScreen';
import ChangePasswordScreen from '../screens/profile/ChangePasswordScreen';
import ProfileScreen from '../screens/profile/ProfileScreen';
import SessionsScreen from '../screens/profile/SessionsScreen';
import { bootstrap } from '../services/auth/authApi';
import { useAuthStore } from '../store/authStore';
import type { AppStackParamList, AuthStackParamList } from './types';

const AuthStack = createNativeStackNavigator<AuthStackParamList>();
const AppStack = createNativeStackNavigator<AppStackParamList>();
const Gate = createNativeStackNavigator();

const AUTO_LOCK_MS = 5 * 60 * 1000;

/** Locks the app after 5 min in the background (shared phones). */
function useAutoLock() {
  useEffect(() => {
    let backgroundAt: number | null = null;
    const sub = AppState.addEventListener('change', state => {
      if (state === 'background') {
        backgroundAt = Date.now();
      } else if (state === 'active') {
        const { status, set } = useAuthStore.getState();
        if (backgroundAt && Date.now() - backgroundAt >= AUTO_LOCK_MS && status === 'signedIn') set({ status: 'locked' });
        backgroundAt = null;
      }
    });
    return () => sub.remove();
  }, []);
}

function AuthScreens() {
  const { t } = useTranslation();
  const languageChosen = useAuthStore(s => s.languageChosen);
  return (
    <AuthStack.Navigator initialRouteName={languageChosen ? 'Login' : 'Welcome'}>
      <AuthStack.Screen name="Welcome" component={WelcomeScreen} options={{ headerShown: false }} />
      <AuthStack.Screen name="Login" component={LoginScreen} options={{ title: t('appName') }} />
      <AuthStack.Screen name="SignUp" component={SignUpScreen} options={{ title: t('signUp.title') }} />
      <AuthStack.Screen name="OtpVerify" component={OtpVerifyScreen} options={{ title: t('otp.title') }} />
      <AuthStack.Screen name="ForgotPassword" component={ForgotPasswordScreen} options={{ title: t('forgot.title') }} />
      <AuthStack.Screen name="ResetPassword" component={ResetPasswordScreen} options={{ title: t('reset.title') }} />
      <AuthStack.Screen name="Activate" component={ActivateScreen} options={{ title: t('activate.title') }} />
    </AuthStack.Navigator>
  );
}

function ProfileButton() {
  const { t } = useTranslation();
  const navigation = useNavigation<NativeStackNavigationProp<AppStackParamList>>();
  return (
    <Pressable onPress={() => navigation.navigate('Profile')} accessibilityRole="button" style={styles.headerButton}>
      <Text style={styles.headerButtonText}>{t('profile.title')}</Text>
    </Pressable>
  );
}

function AppScreens() {
  const { t } = useTranslation();
  return (
    <AppStack.Navigator>
      <AppStack.Screen
        name="Home"
        component={HomeScreen}
        options={{ title: t('appName'), headerRight: ProfileButton }}
      />
      <AppStack.Screen
        name="Module"
        component={ModulePlaceholderScreen}
        options={({ route }) => ({ title: t(`modules.${route.params.module}`) })}
      />
      <AppStack.Screen name="Profile" component={ProfileScreen} options={{ title: t('profile.title') }} />
      <AppStack.Screen name="ChangePassword" component={ChangePasswordScreen} options={{ title: t('password.changeTitle') }} />
      <AppStack.Screen name="Sessions" component={SessionsScreen} options={{ title: t('sessions.title') }} />
    </AppStack.Navigator>
  );
}

export default function RootNavigator() {
  const { t } = useTranslation();
  const status = useAuthStore(s => s.status);
  useAutoLock();
  useEffect(() => {
    bootstrap();
  }, []);

  if (status === 'loading') {
    return (
      <View style={styles.splash}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }
  return (
    <NavigationContainer>
      <Gate.Navigator screenOptions={{ headerShown: false }}>
        {status === 'signedOut' && <Gate.Screen name="Auth" component={AuthScreens} />}
        {status === 'needsPin' && <Gate.Screen name="SetPin" component={SetPinScreen} options={{ headerShown: true, title: t('appName') }} />}
        {status === 'locked' && <Gate.Screen name="Unlock" component={PinUnlockScreen} options={{ headerShown: true, title: t('appName') }} />}
        {status === 'signedIn' && <Gate.Screen name="App" component={AppScreens} />}
      </Gate.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  splash: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.background },
  headerButton: { minHeight: 44, justifyContent: 'center', paddingHorizontal: 8 },
  headerButtonText: { color: colors.primary, fontSize: 17, fontWeight: '600' },
});
