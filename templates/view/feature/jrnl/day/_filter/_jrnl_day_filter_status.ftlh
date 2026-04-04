<#--
 * jrnl_day: filter status (top)
 * @included_in : jrnl_day_*.ftlh
 -->

<div id="jrnl_day_filter_status" class="card post mb-5" style="margin-top: 0 !important;">
    <div class="card-body py-4">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-funnel fs-4 text-primary"></i>
                <span class="fw-bold text-gray-900">Filter status</span>
                <span id="jrnl_day_filter_count" class="badge badge-light text-gray-600">0</span>
            </div>

            <div id="jrnl_day_filter_chip_list" class="d-flex flex-wrap gap-2">
                <span class="text-muted fs-7">No active filters</span>
            </div>

            <div class="d-flex align-items-center">
                <button type="button" class="btn btn-sm btn-light" disabled>
                    Reset
                </button>
            </div>
        </div>
    </div>
</div>
