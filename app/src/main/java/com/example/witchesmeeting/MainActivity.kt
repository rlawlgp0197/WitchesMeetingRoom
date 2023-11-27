package com.example.witchesmeeting

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.example.witchesmeeting.data.Reserve
import com.example.witchesmeeting.databinding.ActivityMainBinding
import com.example.witchesmeeting.extension.addFragment
import com.example.witchesmeeting.rest.Repository
import com.kakao.sdk.user.UserApiClient
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var selectedDay: CalendarDay
    private val scope = MainScope()
    private var userId: String? = null
    private var userNickname: String? = null
    private val reserveList: MutableList<Reserve> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getUserEmail()
        getMonthReserve(CalendarDay.today().year, CalendarDay.today().month + 1)
        selectDay(null)
        userClicked()
        refreshList()
        setMonthReserve()
    }

    private fun getUserEmail() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("getUserId", "사용자 정보 요청 실패", error)
            } else if (user != null) {
                Log.d(
                    "user.kakaoAccount?.profile?.nickname",
                    user.kakaoAccount?.profile?.nickname.toString()
                )
                userId = user.id.toString()
                userNickname = user.kakaoAccount?.profile?.nickname
                inputUser()
            }
        }
    }

    //첫 로그인인지 확인 -> 첫로그인이면 유저테이블에 넣음
    private fun inputUser() {
        val pref = getSharedPreferences("isFirst", MODE_PRIVATE)
        val first = pref.getBoolean("isFirst", false)
        if (!first) {
            Log.d("IsFirstTime?", "first")
            val editor = pref.edit()
            editor.putBoolean("isFirst", true)
            editor.commit()
            val retrofit = Repository.buildWitchesService()
            val call: Call<String>? = retrofit.userLogin(userId, "kakao")
            call!!.enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    val response: String? = response.body()
                    Log.d("userSuccess@@", response.toString())
                }

                override fun onFailure(call: Call<String?>, t: Throwable) {
                    Log.d("userFail@@", t.message.toString())
                }
            })
        } else {
            Log.d("IsFirstTime?", "not first")

        }
    }

    //월 이동시 데이터를 다시 설정하는 함수
    private fun setMonthReserve() {
        binding.calendar.setOnMonthChangedListener { widget, date ->
            var year = date.year
            var month = date.month + 1
            getMonthReserve(year, month)
            // 월이 변경될 때마다 1일로 설정
            val calendar = Calendar.getInstance()
            calendar[Calendar.YEAR] = date.year
            calendar[Calendar.MONTH] = date.month
            calendar[Calendar.DAY_OF_MONTH] = 1

            var firstDay = CalendarDay.from(calendar)
            if (firstDay.month == CalendarDay.today().month) {
                firstDay = CalendarDay.today()

            }
            binding.calendar.clearSelection()
            selectDay(firstDay)
            binding.calendar.setDateSelected(firstDay, true)
        }
    }

    //월별 예약된 리스트 가져오는 함수
    fun getMonthReserve(year: Int?, month: Int?) = scope.launch {
        Repository.getMonthReserve(year, month)?.let { list ->
            reserveList.clear()
            reserveList.addAll(list)
            convertToDate(reserveList)
            deco(convertToDate(reserveList))
            selectDay(selectedDay)
            onDateChange()
        }
    }

    // 아래로 슬라이드시 새로고침 -> 다시 리스트 받아옴
    private fun refreshList() {
        binding.refreshLayout.setOnRefreshListener {
            getMonthReserve(selectedDay.year, selectedDay.month + 1)
            selectDay(selectedDay)
            Log.d("selectedDayMonth@@", selectedDay.month.toString())
            binding.refreshLayout.isRefreshing = false
        }

    }

    // 받아온 날짜 int데이터 -> calendar데이터로 변환
    private fun convertToDate(reserveList: List<Reserve>): Collection<CalendarDay> {
        val calendarDays = mutableListOf<CalendarDay>()
        reserveList.forEach { reserve ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, reserve.year)
                set(Calendar.MONTH, reserve.month - 1) // 0부터 시작하므로 월-1
                set(Calendar.DAY_OF_MONTH, reserve.day)
            }
            calendarDays.add(CalendarDay.from(calendar))
        }
        var convertCalendarDays = calendarDays
        Log.d("convertCalendarDays@@", convertCalendarDays.toString())
        return convertCalendarDays
    }

    //이전날짜 색상 설정
    inner class DayDisableDecorator : DayViewDecorator {
        private var dates = HashSet<CalendarDay>()
        var today: CalendarDay

        constructor(dates: HashSet<CalendarDay>, today: CalendarDay) {
            this.dates = dates
            this.today = today
        }

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day.isBefore(today)
        }

        override fun decorate(view: DayViewFacade?) {
            view?.let {
                it.setDaysDisabled(true)
                it.addSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.disable
                        )
                    )
                )
            }
        }
    }


    // 예약이 있는 날 데코 설정
    inner class EventDecorator(dates: Collection<CalendarDay>) : DayViewDecorator {
        var dates: HashSet<CalendarDay> = HashSet(dates)

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(DotSpan(10F, Color.parseColor("#4FD155")))
        }
    }

    // 설정한 데코 적용
    private fun deco(reservedCollection: Collection<CalendarDay>) {
        val today = CalendarDay.today()
        val disabledDates = hashSetOf<CalendarDay>()
        // 데코된거 모두 삭제 후 이전날짜, 예약날짜 데코설정
        binding.calendar.apply {
            removeDecorators()
            addDecorator(DayDisableDecorator(disabledDates, today))
            addDecorator(EventDecorator(reservedCollection))
        }
    }

    // 내가 선택한 날짜
    private fun selectDay(date: CalendarDay?) = with(binding) {
        // 선택한 날이 없으면 오늘 날짜
        if (date == null) {
            selectedDay = CalendarDay.today()
            calendar.setDateSelected(selectedDay, true)
        } else selectedDay = date

        // 인덱스이므로 실제 월을 구하기 위해 1 더해줌
        var month = selectedDay.month + 1
        var filterList = mutableListOf<Reserve>()
        reserveList.forEach { listData ->
            if (selectedDay.year == listData.year && month == listData.month && selectedDay.day == listData.day) {
                if (listData.createNm == userId) {
                    listData.isMe = true
                }
                filterList.add(listData)
            }
        }
        // 선택시 나오는 fragment 설정
        var reservedFragment = ReservedFragment().apply {
            arguments = bundleOf(
                "year" to selectedDay.year.toString(),
                "month" to month.toString(),
                "day" to selectedDay.day.toString(),
            )
            this.reserveList = filterList
        }
        binding.container.isVisible = true
        addFragment(R.id.container, reservedFragment, addBackStack = false)
    }

    // 날짜 클릭 리스너
    private fun onDateChange() = with(binding) {
        calendar.setOnDateChangedListener { widget, date, selected ->
            selectedDay = calendar.selectedDate
            var date = calendar.selectedDate
            var month = date.month + 1
            // 월별로 가져온 데이터 누른날짜와 비교해서 같은날짜만 뽑아옴
            var filterList = mutableListOf<Reserve>()
            reserveList.forEach { listData ->
                if (date.year == listData.year && month == listData.month && date.day == listData.day) {
                    if (listData.createNm == userId) {
                        listData.isMe = true
                    }
                    Log.d("123123", listData.isMe.toString())
                    filterList.add(listData)

                }
            }
            var reservedFragment = ReservedFragment().apply {
                arguments = bundleOf(
                    "year" to date.year.toString(),
                    "month" to month.toString(),
                    "day" to date.day.toString(),
                    "selectedDay" to selectedDay.toString()
                )
                this.reserveList = filterList

            }
            binding.container.isVisible = true
            addFragment(R.id.container, reservedFragment, addBackStack = false)
        }

    }

    private fun userClicked() {
        binding.user.setOnClickListener {
            val saveNameDialog = SaveNameDialog().apply {
            }
            saveNameDialog.show(supportFragmentManager, null)
        }
    }


}











