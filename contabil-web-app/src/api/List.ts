import axios from 'axios'
import Activity from '../interfaces/Activity'

const url = `${process.env.REACT_APP_BACKEND_API}`

export const getList = async (id: string) => {
    const responseExpense = await axios.get<Activity[]>(
        url + '/expense/all/' + id
    )
    const responseIncome = await axios.get<Activity[]>(
        url + '/income/all/' + id
    )

    return [...responseExpense.data, ...responseIncome.data]
}

export const deleteActivity = async (bucket: Activity[]) => {
    const ExpenseBucket = bucket.filter(
        (activity) => activity.type === 'Expense'
    )
    const IncomeBucket = bucket.filter((activity) => activity.type === 'Income')

    await axios.post(
        url + '/expense/delete/bucket',
        ExpenseBucket.map((activity) => activity.id)
    )
    await axios.post(
        url + '/income/delete/bucket',
        IncomeBucket.map((activity) => activity.id)
    )

    return true
}
