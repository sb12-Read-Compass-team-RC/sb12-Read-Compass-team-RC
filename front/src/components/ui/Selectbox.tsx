import { useClickOutside } from "@/hooks/common/useClickOutside";
import clsx from "clsx";
import { useState, useEffect } from "react";
import getImagePath from "@/constants/images.ts";

interface SelectOption<T> {
  value: T;
  label: string;
}

interface SelectBoxProps<T> {
  options: readonly SelectOption<T>[];
  value: T;
  onChange: (value: T) => void;
}

export default function Selectbox<T extends string>({
  options,
  value,
  onChange
}: SelectBoxProps<T>) {
  const [buttonValue, setButtonValue] = useState(
    options.find(o => o.value === value)?.label || options[0].label
  );

  const { open, setOpen, dropdownRef } = useClickOutside();

  useEffect(() => {
    const selected = options.find(o => o.value === value);
    if (selected) setButtonValue(selected.label);
  }, [value, options]);

  return (
    <div className="relative" ref={dropdownRef}>
      <div
        onClick={() => setOpen(prev => !prev)}
        className="relative px-[18px] pr-[40px] h-[46px] border border-gray-300 rounded-full flex items-center justify-center text-gray-600 font-medium cursor-pointer"
      >
        <span className="block w-full text-center truncate">{buttonValue}</span>
        <img
          src={getImagePath("/icon/ic_chevron-down.svg")}
          alt="보기"
          width={18}
          height={18}
          className="absolute right-[14px]"
        />
      </div>

      {open && (
        <ul className="absolute left-0 mt-2 w-full max-h-[260px] bg-white border border-gray-200 rounded-2xl shadow-lg z-10 overflow-y-auto text-center text-gray-600 font-medium">
          {options.map(opt => (
            <li
              key={opt.value}
              onClick={() => {
                onChange(opt.value);
                setButtonValue(opt.label);
                setOpen(false);
              }}
              className={clsx(
                "px-3 py-4 cursor-pointer duration-[.2s]",
                "hover:bg-gray-100"
              )}
            >
              {opt.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
