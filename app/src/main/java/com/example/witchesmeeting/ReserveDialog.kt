package com.example.witchesmeeting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.witchesmeeting.data.CalendarDTO
import com.example.witchesmeeting.data.Reserve
import com.example.witchesmeeting.databinding.DialogReserveBinding
import com.example.witchesmeeting.rest.Repository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalTime
import java.time.format.DateTimeFormatter

//Android 8.0 (Oreo) 이상 에서만 가능
//LocalTime으로 변환하여 선택시간 값을 필터링을 해야해서 사용함
@RequiresApi(Build.VERSION_CODES.O)
class ReserveDialog : BottomSheetDialogFragment() {
    private var _binding: DialogReserveBinding? = null

    private val binding get() = _binding!!
    private lateinit var activity: MainActivity

    private var year: Int? = null
    private var month: Int? = null
    private var day: Int? = null
    private var start: String? = null
    private var end: String? = null
    private var userId: String? = null
    private var department: String? = null
    private var name: String? = null

    var selectedStart: String? = null

    var dayList = listOf<Reserve>()


    private val departmentSpinnerAdapter by lazy {
        spinnerAdapter(R.array.departmentList)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        name = arguments?.getString("name")
        year = arguments?.getString("year")?.toInt()
        month = arguments?.getString("month")?.toInt()
        day = arguments?.getString("day")?.toInt()

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = DialogReserveBinding.inflate(inflater, container, false)
        return binding.root

    }


    //SuppressLint = 경고를 무시하고 하드코딩된 문자열을 사용함
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSpinner()
        endSpinner()
        departmentSpinner()
        getUserId()
        binding.nameEdit.text = Editable.Factory.getInstance().newEditable(name)
        binding.dayText.text = "${year}년 ${month}월 ${day}일"
        postReserve()
    }

    private fun getUserId() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("getEmail", "사용자 정보 요청 실패", error)
            } else if (user != null) {
                userId = user.id.toString()
            }
        }
    }

    // res/array -> ArrayList로 변환
    private fun getStringArray(resourceId: Int): List<String> {
        val stringArray: Array<String> = resources.getStringArray(resourceId)
        return stringArray.toList()
    }

    // 시작 스피너 아이템 array 필터링
    private fun filterStart(): ArrayList<String> {
        val startList: ArrayList<String> = ArrayList(getStringArray(R.array.startTimeList))
        //예약된 시작시간과 종료시간 사이에 있는 시간 제외
        val notMatchingTimes: ArrayList<String> = startList.filter { time ->
            val targetLocalTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            dayList.none { remove ->
                val startTime = LocalTime.parse(remove.start, DateTimeFormatter.ofPattern("HH:mm"))
                val endTime = LocalTime.parse(remove.end, DateTimeFormatter.ofPattern("HH:mm"))
                targetLocalTime.isAfter(startTime) && targetLocalTime.isBefore(endTime)
            }
        } as ArrayList<String>


        //예약된 시작시간 제외
        val startFilterList: ArrayList<String> = notMatchingTimes.filter { startListItem ->
            dayList.none { dayItem ->
                startListItem.contains(dayItem.start)
            }
        } as ArrayList<String>

        Log.d("filterStart", startFilterList.toString())
        return startFilterList
    }

    // 종료 스피너 아이템 array 필터링
    private fun filterEnd(select: String?): ArrayList<String> {
        val endList: ArrayList<String> = ArrayList(getStringArray(R.array.endTimeList))
        selectedStart = select ?: "00:00"
        val selectedStartTime = LocalTime.parse(selectedStart, DateTimeFormatter.ofPattern("HH:mm"))
        Log.d("selectedStartTime", selectedStartTime.toString())

        // 시작시간 이후의 list가져오기
        val endFilterList: ArrayList<String> = endList.filter { endListItem ->
            val endTime = LocalTime.parse(endListItem, DateTimeFormatter.ofPattern("HH:mm"))
            endTime.isAfter(selectedStartTime)
        } as ArrayList<String>

        //예약된 시작시간과 종료시간 사이에 있는 시간 제외
        val notMatchingTimes: ArrayList<String> = endFilterList.filter { time ->
            val targetLocalTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            dayList.none { item ->
                val startTime = LocalTime.parse(item.start, DateTimeFormatter.ofPattern("HH:mm"))
                val endTime = LocalTime.parse(item.end, DateTimeFormatter.ofPattern("HH:mm"))
                targetLocalTime.isAfter(startTime) && targetLocalTime.isBefore(endTime)
            }
        } as ArrayList<String>

        // 필터링된 리스트 중에서 이미 예약된 종료시간 삭제
        val endReFilterList: ArrayList<String> = notMatchingTimes.filter { endFilterListItem ->
            dayList.none { item ->
                endFilterListItem.contains(item.end)
            }
        } as ArrayList<String>

        Log.d("filterItemStartTime", endFilterList.toString())
        return endReFilterList
    }

    // 시간이 들어가는 스피너(필터링 하려고 따로 만듦)
    private fun timeSpinnerAdapter(arrayList: ArrayList<String>): ArrayAdapter<CharSequence> {
        return ArrayAdapter(
            requireContext(),
            R.layout.item_time_choice,
            arrayList as List<CharSequence>
        )
    }

    // 일반 스피너 어댑터
    private fun spinnerAdapter(resourceId: Int): ArrayAdapter<CharSequence> {
        return ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_choice,
            resources.getStringArray(resourceId)
        )
    }

    // 시작시간 스피너
    private fun startSpinner() = with(binding) {
        start.adapter = timeSpinnerAdapter(filterStart())
        start.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // 스피너의 원래 폰트가 맘에 안들어서 폰트 변경을 위해 invisible
                view?.visibility = View.INVISIBLE
                // 보이는 text변경
                startText.text = start.selectedItem.toString()

                // 선택한 아이템의 string을 기반으로 endSpinner아이템 업데이트
                updateEndSpinner(start.selectedItem.toString())
                Log.d("startText.text", start.selectedItem.toString())
                this@ReserveDialog.start = startText.text.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    }

    // 시작시간 스피너 선택시 종료시간 스피너아이템 업데이트
    private fun updateEndSpinner(selectedStart: String?) {
        binding.end.adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_time_choice,
            filterEnd(selectedStart) as List<CharSequence>
        )
    }

    // 종료시간 스피너
    private fun endSpinner() = with(binding) {
        updateEndSpinner(null)
        end.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
//                val selectedText = parent?.getItemAtPosition(position).toString()
                view?.visibility = View.INVISIBLE
                endText.text = end.selectedItem.toString()
                this@ReserveDialog.end = endText.text.toString()
//                startText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    }

    // 부서선택 스피너
    private fun departmentSpinner() = with(binding) {
        departmentSpinner.adapter = departmentSpinnerAdapter
        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                view?.visibility = View.INVISIBLE
                departmentSpinnerText.text = departmentSpinner.selectedItem.toString()
                this@ReserveDialog.department = departmentSpinnerText.text.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                departmentSpinnerText.text = "부서선택"
            }
        }
    }


    // 예약 완료버튼 클릭
    private fun postReserve() {
        binding.sendBtn.setOnClickListener {
            setCalendarDTO()
            var isNull = isNull(setCalendarDTO())
            if (!isNull) {
                var reservedCheck = isReservedTime(setCalendarDTO().start, setCalendarDTO().end)
                if (reservedCheck) {
                    postData(setCalendarDTO())
                    Toast.makeText(requireContext(), "예약 성공", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "이미 예약된 시간입니다.", Toast.LENGTH_SHORT).show()
                }

            } else {
                it.isEnabled = false
                Toast.makeText(requireContext(), "필수 입력값을 작성해 주세요.", Toast.LENGTH_SHORT).show()
                it.isEnabled = true
            }
        }
    }

    // post에 보낼 CalendarDTO 세팅
    private fun setCalendarDTO(): CalendarDTO {
        val peopleNum = binding.peopleNum.text.toString()
        val contents = binding.contentsEdit.text.toString()
        val name = binding.nameEdit.text.toString()
        val calendarDTO = CalendarDTO(
            start,
            end,
            peopleNum,
            name,
            department,
            contents,
            year,
            month,
            day,
            userId
        )
        Log.d("calendarDTO", calendarDTO.toString())
        return calendarDTO
    }

    // retrofit으로 보낼 객체 null체크
    private fun isNull(calendarDTO: CalendarDTO): Boolean {
        val result: Boolean =
            calendarDTO.start == null || calendarDTO.start == "시작시간" || calendarDTO.end == null || calendarDTO.end == "종료시간" || calendarDTO.peopleNum?.isEmpty() == true ||
                    calendarDTO.name?.isEmpty() == true || calendarDTO.department == null || calendarDTO.department == "부서선택" || calendarDTO.contents?.isEmpty() == true ||
                    calendarDTO.year == null || calendarDTO.month == null || calendarDTO.day == null || userId == null
        return result
    }

    // 시작시간과 종료시간 사이에 이미 예약된 시간이 포함되어 있는지 판단
    private fun isReservedTime(startTime: String?, endTime: String?): Boolean {
        val startList: ArrayList<String> = ArrayList(getStringArray(R.array.startTimeList))

        //예약된 시작시간과 종료시간 사이에 있는 시간만
        val matchingTimes: ArrayList<String> = startList.filter { time ->
            val targetLocalTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            val startT = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"))
            val endT = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"))
            targetLocalTime.isAfter(startT) && targetLocalTime.isBefore(endT)
        } as ArrayList<String>
        Log.d("matchingTime@@", matchingTimes.toString())

        // 한번 필터링한 리스트 n dayList안에 있는 time
        val invalidReserveList = matchingTimes.filter { matchingTime ->
            dayList.any {
                matchingTime == it.start || matchingTime == it.end
            }
        }
        Log.d("filterList@@", invalidReserveList.toString())

        // start와 end사이에 invalidReserveList가 존재하는가
        val resultList: ArrayList<String> = invalidReserveList.filter { time ->
            val targetLocalTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            val startT = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"))
            val endT = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"))
            targetLocalTime.isAfter(startT) && targetLocalTime.isBefore(endT)
        } as ArrayList<String>

        // 비어있어야 예약가능
        val result = resultList.isEmpty()

        return result

    }

    // retrofit으로 post
    private fun postData(calendarDTO: CalendarDTO) {
        val retrofit = Repository.buildWitchesService()
        val call: Call<String?>? = retrofit.postData(calendarDTO)

        call!!.enqueue(object : Callback<String?> {
            override fun onResponse(
                call: Call<String?>,
                response: Response<String?>
            ) {
                val response: String? = response.body()
                Log.d("success@@", response.toString())
                activity.getMonthReserve(year, month)
                Log.d("successMonth@@", month.toString())
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                Log.d("Fail@@", t.message.toString())
            }
        })
    }


//        val retrofit = Retrofit.Builder()
//            .baseUrl("url")
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
//            .build()

//        val retrofitAPI = retrofit.create(WitchesService::class.java)
//        val call: Call<String?>? = retrofitAPI.postData(Gson().toJson(calendarDTO, CalendarDTO::class.java))
}



