import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const nickname = ref('')
  const roles = ref<string[]>([])

  function setToken(value: string) {
    token.value = value
    localStorage.setItem('token', value)
  }

  function setProfile(nick: string, roleList: string[]) {
    nickname.value = nick
    roles.value = roleList
  }

  function logout() {
    token.value = ''
    nickname.value = ''
    roles.value = []
    localStorage.removeItem('token')
  }

  return { token, nickname, roles, setToken, setProfile, logout }
})
