package com.example.witchesmeeting


import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.witchesmeeting.adapter.ReserveAdapter
import com.example.witchesmeeting.data.Reserve
import com.example.witchesmeeting.databinding.FragmentReservedBinding
import java.time.LocalDate

class ReservedFragment : Fragment() {
    private var _binding: FragmentReservedBinding? = null
    private val binding get() = _binding!!
    private var reserveAdapter = ReserveAdapter()
    private var year: Int? = null
    private var month: Int? = null
    private var day: Int? = null
    private lateinit var selectedDay: String
    private var isBefore = false

    var reserveList = listOf<Reserve>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        year = arguments?.getString("year")?.toInt()
        month = arguments?.getString("month")?.toInt()
        day = arguments?.getString("day")?.toInt()
        selectedDay = arguments?.getString("selectedDay").toString();
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReservedBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        Log.d("selectedDay@@", selectedDay)
        onClicked()
        binding.dayText.text = "${year}년 ${month}월 ${day}일"
    }


    // 날짜클릭시 나오는 리사이클러뷰 설정
    private fun initView() = with(binding) {
        dayMeetingRv.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )
        dayMeetingRv.adapter = reserveAdapter
        reserveAdapter.removeClickListener = object : ReserveAdapter.OnRemoveClickListener {
            override fun onRemoveClick(position: Int) {
                val item = reserveList[position]
                Log.d("onRemoveClick@@", item.toString())
                val removeDialog = RemoveDialog().apply {
                    arguments =
                        bundleOf(
                            "reservedId" to item.id.toString(),
                            "month" to month.toString(),
                            "year" to year.toString()
                        )
                    Log.d("month$$", month.toString())

                }
                Log.d("item.id.toString()", item.id.toString())
                removeDialog.show(childFragmentManager, null)

            }
        }

        reserveAdapter.onItemClickListener = object : ReserveAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val item = reserveList[position]
                Log.d("onItemClick@@", item.toString())
                reserveAdapter.updateIsClicked(position, !item.isClicked)
            }
        }

        // 예약프래그먼트 or 예약된 회의가 없습니다
        binding.emptyText.isVisible = reserveList.isEmpty()
        binding.dayMeetingRv.visibility =
            if (reserveList.isEmpty()) View.INVISIBLE else View.VISIBLE

        reserveAdapter.set(reserveList)
    }


    // LocalDate 사용으로 인한 어노테이션
    // 이전날짜인지 확인
    @RequiresApi(Build.VERSION_CODES.O)
    private fun isBefore(): Boolean {
        val today = LocalDate.now()
        val comparisonDate = year?.let { year ->
            month?.let { month ->
                day?.let { day ->
                    LocalDate.of(year, month, day)
                }
            }
        }
        if (comparisonDate != null) {
            isBefore = comparisonDate.isBefore(today)
        }
        return isBefore
    }

    //이전날짜 예약불가 오늘 포함 이후 예약하기
    @RequiresApi(Build.VERSION_CODES.O)
    private fun onClicked() {
        binding.reserveBtn.isEnabled = !isBefore()
        if (isBefore()) {
            binding.reserveBtn.text = "예약불가"
        } else {
            binding.reserveBtn.setOnClickListener {
                it.isEnabled = false
                val bottomDialog = ReserveDialog().apply {
                    arguments = bundleOf(
                        "year" to year.toString(),
                        "month" to month.toString(),
                        "day" to day.toString(),
                        "name" to GlobalApplication.prefsManager.getString("name", ""),
                    )
                    this.dayList = reserveList
                }
                Log.d("123123@@@", GlobalApplication.prefsManager.getString("name", ""))
                bottomDialog.show(childFragmentManager, null)
                it.isEnabled = true
            }
        }
    }
}

