package com.example.tripsync

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.AuthService
import com.example.tripsync.databinding.FragmentCommunityBinding

class CommunityFragment : Fragment() {
//    private lateinit var binding: FragmentCommunityBinding
//    private lateinit var viewModel: CommunityViewModel
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        binding = FragmentCommunityBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val api = ApiClient.createService(requireContext(), AuthService::class.java)
//        val repository = CommunityRepository(api)
//        viewModel = ViewModelProvider(this, CommunityViewModelFactory(repository))
//            .get(CommunityViewModel::class.java)
//
//        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
//
//        viewModel.posts.observe(viewLifecycleOwner) { posts ->
//            binding.recyclerView.adapter = PostAdapter(posts)
//        }
//
//        viewModel.fetchPosts()
//    }
}
