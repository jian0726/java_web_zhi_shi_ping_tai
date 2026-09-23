import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')
  const roles = ref<string[]>(JSON.parse(localStorage.getItem('roles') || '[]'))

  function setToken(value: string) {
    token.value = value
    localStorage.setItem('token', value)
  }

  function setProfile(nick: string, roleList: string[]) {
    nickname.value = nick
    roles.value = roleList
    localStorage.setItem('nickname', nick)
    localStorage.setItem('roles', JSON.stringify(roleList))
  }

  function hasRole(role: string) {
    return roles.value.includes(role)
  }

  function logout() {
    token.value = ''
    nickname.value = ''
    roles.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('nickname')
    localStorage.removeItem('roles')
  }

  return { token, nickname, roles, setToken, setProfile, hasRole, logout }
})
