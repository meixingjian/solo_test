import request from '@/utils/request'

export function getProjects() {
  return request({
    url: '/admin/projects',
    method: 'get'
  })
}

export function getProjectById(id) {
  return request({
    url: `/admin/projects/${id}`,
    method: 'get'
  })
}

export function addProject(data) {
  return request({
    url: '/admin/projects',
    method: 'post',
    data
  })
}

export function updateProject(id, data) {
  return request({
    url: `/admin/projects/${id}`,
    method: 'put',
    data
  })
}

export function deleteProject(id) {
  return request({
    url: `/admin/projects/${id}`,
    method: 'delete'
  })
}

export function toggleProjectStatus(id) {
  return request({
    url: `/admin/projects/${id}/toggle-status`,
    method: 'post'
  })
}
