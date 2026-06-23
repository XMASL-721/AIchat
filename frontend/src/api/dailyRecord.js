import request from './request'

export function addRecordApi(recordType, targetId, targetName, calories) {
  return request.post('/records', { recordType, targetId, targetName, calories })
}

export function getUserRecordsApi(startDate, endDate, recordType, page = 1, size = 20) {
  return request.get('/records', { params: { startDate, endDate, recordType, page, size } })
}

export function getTodaySummaryApi() {
  return request.get('/records/today')
}

export function getDateSummaryApi(date) {
  return request.get('/records/summary', { params: { date } })
}

export function getWeeklyStatsApi() {
  return request.get('/records/weekly')
}

export function getCategoryStatsApi(startDate, endDate) {
  return request.get('/records/categories', { params: { startDate, endDate } })
}
