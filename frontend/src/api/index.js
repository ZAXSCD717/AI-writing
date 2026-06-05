import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 120000 // 2min for AI conversion
})

// Response interceptor for unified error handling
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      console.error('API error:', res.message)
      return Promise.reject(new Error(res.message))
    }
    return res.data
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

export default request
