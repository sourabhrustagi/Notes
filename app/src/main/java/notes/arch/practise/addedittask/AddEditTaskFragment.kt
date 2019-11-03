package notes.arch.practise.addedittask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import notes.arch.practise.EventObserver
import notes.arch.practise.R
import notes.arch.practise.databinding.AddtaskFragBinding
import notes.arch.practise.tasks.ADD_EDIT_RESULT_OK
import notes.arch.practise.util.setupRefreshLayout
import notes.arch.practise.util.setupSnackbar
import javax.inject.Inject

class AddEditTaskFragment : DaggerFragment() {
    private lateinit var viewDataBinding: AddtaskFragBinding

    private val args: AddEditTaskFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<AddEditTaskViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.addtask_frag, container, false)
        viewDataBinding = AddtaskFragBinding.bind(root).apply {
            this.viewmodel = viewModel
        }
        // Set the lifecycle owner to the lifecycle of the view
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSnackbar()
        setupNavigation()
        this.setupRefreshLayout(viewDataBinding.refreshLayout)
        viewModel.start(args.taskId)
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarMessage, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        viewModel.taskUpdatedEvent.observe(this, EventObserver {
            val action = AddEditTaskFragmentDirections
                .actionAddEditTaskFragmentToTasksFragment(ADD_EDIT_RESULT_OK)
            findNavController().navigate(action)
        })
    }
}
