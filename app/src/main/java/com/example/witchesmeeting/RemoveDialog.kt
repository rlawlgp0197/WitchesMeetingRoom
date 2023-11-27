package com.example.witchesmeeting

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.witchesmeeting.data.CalendarDTO
import com.example.witchesmeeting.data.Remove
import com.example.witchesmeeting.data.Reserve
import com.example.witchesmeeting.data.ResponseString
import com.example.witchesmeeting.databinding.DialogRemoveCheckBinding
import com.example.witchesmeeting.rest.Repository
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RemoveDialog : DialogFragment() {
    private var _binding: DialogRemoveCheckBinding? = null
    private val binding get() = _binding!!
    private var userId: String? = null
    private var reservedId: String? = null
    private var convertReservedId: Int? = null
    private var name: String? = null
    private var month: String? = null
    private var year: String? = null

    private lateinit var activity: MainActivity


    //취소시 메인액티비티의 함수로 데이터 리스트를 다시 요청하기 위해
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reservedId = arguments?.getString("reservedId");
        year = arguments?.getString("year")
        month = arguments?.getString("month")
        name = GlobalApplication.prefsManager.getString("name", "")
        convertReservedId = reservedId?.toInt()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogRemoveCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUserId()
        // 레이아웃 배경 투명
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        btnClick()
        binding.cancleNameEdit.text = Editable.Factory.getInstance().newEditable(name)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun btnClick() {
        // 취소 버튼
        binding.noButton.setOnClickListener {
            dismiss()
        }
        // 확인 버튼 클릭
        binding.yesButton.setOnClickListener {
            setRemove()
            val isNull = isNull(setRemove())
            if (!isNull) {
                patchRemove(setRemove())
                dismiss()
                Toast.makeText(context, "취소 완료", Toast.LENGTH_SHORT).show()
            } else {
                it.isEnabled = false
                Toast.makeText(context, "취소자를 작성해 주세요.", Toast.LENGTH_SHORT).show()
                it.isEnabled = true
            }
        }
    }


    // 카카오 유저 아이디 가져옴
    private fun getUserId() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("getUserId@@", "사용자 정보 요청 실패", error)
            } else if (user != null) {
                Log.d("getUserId@@", user.id.toString())
                userId = user.id.toString()
            }
        }
    }

    //Remove 세팅
    private fun setRemove(): Remove {
        var reason = binding.reasonEdit.text.toString()
        var cancleName = binding.cancleNameEdit.text.toString()
        var remove = Remove(convertReservedId, userId, cancleName, reason, userId)
        Log.d("Remove@@", remove.toString())
        return remove
    }

    //Remove에 null 값이 있는지 판단
    private fun isNull(remove: Remove): Boolean {
        var result: Boolean =
            convertReservedId == null || userId == null || remove.cancleName?.isEmpty() == true
        return result
    }

    // retrofit patch
    private fun patchRemove(remove: Remove) {
        val retrofit = Repository.buildWitchesService()
        val call: Call<String?>? = retrofit.deleteData(remove)

        call!!.enqueue(object : Callback<String?> {
            override fun onResponse(
                call: Call<String?>,
                response: Response<String?>
            ) {
                val response: String? = response.body()
                activity.getMonthReserve(year?.toInt(), month?.toInt())
                Log.d("retrofitSuccess@@", response.toString())

            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                Log.d("retrofitFail@@", call.toString())
                Log.d("retrofitFail@@", t.message.toString())
            }
        })
    }


}




