import 'react-native-gesture-handler';
import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  Button,
  StyleSheet,
  StatusBar,
  useColorScheme,
} from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import axios from 'axios';
import { SafeAreaProvider } from 'react-native-safe-area-context';

// 📌 API 호출 함수
const fetchData = async (endpoint: string) => {
  try {
    const response = await axios.get(`https://jsonplaceholder.typicode.com/${endpoint}`);
    console.log(`${endpoint} 데이터:`, response.data);
  } catch (error) {
    console.error(`${endpoint} API 호출 실패:`, error);
  }
};
// 📌 메인 탭
const MainScreen = () => (
  <View style={styles.container}>
    <Text style={styles.title}>🏠 메인 화면</Text>
    <Button title="API 호출" onPress={() => fetchData('posts')} />
  </View>
);

// 📌 커뮤니티 탭
const CommunityScreen = () => (
  <View style={styles.container}>
    <Text style={styles.title}>💬 커뮤니티 화면</Text>
    <Button title="API 호출" onPress={() => fetchData('comments')} />
  </View>
);

// 📌 로그 탭
const LogScreen = () => (
  <View style={styles.container}>
    <Text style={styles.title}>📜 로그 화면</Text>
    <Button title="API 호출" onPress={() => fetchData('users')} />
  </View>
);

// 📌 탭 네비게이터 (로그인 후 이동)
const Tab = createBottomTabNavigator();

const MainTabs = () => (
  <Tab.Navigator>
    <Tab.Screen name="Main" component={MainScreen} />
    <Tab.Screen name="Community" component={CommunityScreen} />
    <Tab.Screen name="Logs" component={LogScreen} />
  </Tab.Navigator>
);
// 📌 로그인 화면
const LoginScreen = ({ navigation }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const isDarkMode = useColorScheme() === 'dark';

  const handleLogin = () => {
    console.log(`로그인 시도: ${email}, ${password}`);
    // 로그인 성공 시 탭으로 이동
    navigation.replace('MainTabs');
  };

  return (
    <View style={[styles.container, { backgroundColor: isDarkMode ? '#222' : '#fff' }]}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <Text style={styles.title}>로그인</Text>
      <TextInput
        style={styles.input}
        placeholder="이메일 입력"
        placeholderTextColor="#aaa"
        value={email}
        onChangeText={setEmail}
      />
      <TextInput
        style={styles.input}
        placeholder="비밀번호 입력"
        placeholderTextColor="#aaa"
        secureTextEntry
        value={password}
        onChangeText={setPassword}
      />
      <Button title="로그인" onPress={handleLogin} />
    </View>
  );
};
// 📌 Stack Navigator (로그인 → 탭 네비게이션)
const Stack = createStackNavigator();

export default function App() {
  return (
    <SafeAreaProvider>
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Login">
        <Stack.Screen
          name="Login"
          component={LoginScreen}
          options={{ headerShown: false }}
        />
        <Stack.Screen
          name="MainTabs"
          component={MainTabs}
          options={{ headerShown: false }}
        />
      </Stack.Navigator>
    </NavigationContainer>
    </SafeAreaProvider>
  );
}

// 📌 스타일
const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  input: {
    width: '100%',
    height: 50,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
    paddingHorizontal: 15,
    marginBottom: 15,
    backgroundColor: '#f9f9f9',
  },
});
