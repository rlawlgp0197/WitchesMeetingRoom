package com.example.witchesmeeting

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.witchesmeeting.databinding.DialogSaveNameBinding

class SaveNameDialog:DialogFragment() {
    private var _binding: DialogSaveNameBinding? =null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogSaveNameBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getName()
        saveName()
    }

    // 저장해둔 이름 가져오기
    private fun getName(){
        var name = GlobalApplication.prefsManager.getString("name","")
        binding.nameEdit.text = Editable.Factory.getInstance().newEditable(name)
    }

    // 신청 및 취소에 사용할 이름 저장
    private fun saveName(){
        binding.saveBtn.setOnClickListener {
            GlobalApplication.prefsManager.setString("name",binding.nameEdit.text.toString())
            dismiss()
        }
    }



}