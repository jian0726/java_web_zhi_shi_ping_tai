import request from '@/utils/request'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface JobCognition {
  id: number
  jobName: string
  jobDuty?: string
  jobRequire?: string
  abilityModel?: string
  workScene?: string
}

export function getJobListApi() {
  return request.get<any, ApiResult<JobCognition[]>>('/api/job/list')
}

export function getJobDetailApi(id: string | number) {
  return request.get<any, ApiResult<JobCognition>>(`/api/job/${id}`)
}
